package persistence.dao;

import domain.Post;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;

public class PostDAO {

    private Post mapPost(ResultSet rs) throws SQLException {
        Post post = new Post();

        post.setPostId(rs.getLong("POST_ID"));
        post.setWriterId(rs.getString("WRITER_ID"));
        post.setCategoryId(rs.getObject("CATEGORY_ID") == null ? null : rs.getLong("CATEGORY_ID"));
        post.setTitle(rs.getString("TITLE"));
        post.setSubject(rs.getString("SUBJECT"));
        post.setDescription(rs.getString("DESCRIPTION"));
        post.setPrice(rs.getObject("PRICE") == null ? null : rs.getInt("PRICE"));
        post.setAvailable(rs.getString("AVAILABLE"));
        post.setAvailableTime(rs.getString("AVAILABLE_TIME"));
        post.setStatus(rs.getString("STATUS"));
        post.setViewCount(rs.getObject("VIEW_COUNT") == null ? null : rs.getInt("VIEW_COUNT"));
        post.setFavoriteCount(
                rs.getObject("FAVORITE_COUNT") == null ? null : rs.getInt("FAVORITE_COUNT"));

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");

        if (createdAt != null) {
            post.setCreatedAt(createdAt.toLocalDateTime());
        }

        return post;
    }

    public List<Post> findAll() throws SQLException {
        String sql = "SELECT * FROM POST ORDER BY CREATED_AT DESC, POST_ID DESC";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                posts.add(mapPost(rs));
            }
        }

        return posts;
    }

    public List<Post> findOnSale() throws SQLException {
        String sql =
                "SELECT * FROM POST WHERE STATUS = 'ON_SALE' "
                        + "ORDER BY CREATED_AT DESC, POST_ID DESC";

        List<Post> posts = new ArrayList<>();

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                posts.add(mapPost(rs));
            }
        }

        return posts;
    }

    public Post findById(Long postId) throws SQLException {
        String sql = "SELECT * FROM POST WHERE POST_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapPost(rs);
                }
            }
        }

        return null;
    }

    public List<Post> findByWriterId(String writerId) throws SQLException {
        String sql =
                "SELECT * FROM POST WHERE WRITER_ID = ? "
                        + "ORDER BY CREATED_AT DESC, POST_ID DESC";

        List<Post> posts = new ArrayList<>();

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, writerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapPost(rs));
                }
            }
        }

        return posts;
    }

    public List<Post> search(
            String keyword,
            String classification,
            String college,
            Integer minPrice,
            Integer maxPrice,
            String sort)
            throws SQLException {

        StringBuilder sql =
                new StringBuilder(
                        "SELECT P.* FROM POST P "
                                + "LEFT JOIN CATEGORY C ON P.CATEGORY_ID = C.CATEGORY_ID "
                                + "WHERE 1 = 1");

        List<Object> parameters = new ArrayList<>();
        List<Post> posts = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(
                    " AND (LOWER(P.TITLE) LIKE LOWER(?) " + "OR LOWER(P.SUBJECT) LIKE LOWER(?))");

            parameters.add("%" + keyword.trim() + "%");
            parameters.add("%" + keyword.trim() + "%");
        }

        if (classification != null && !classification.isBlank()) {
            sql.append(" AND C.CLASSIFICATION = ?");
            parameters.add(classification);
        }

        if (college != null && !college.isBlank()) {
            sql.append(" AND C.COLLEGE = ?");
            parameters.add(college);
        }

        if (minPrice != null) {
            sql.append(" AND P.PRICE >= ?");
            parameters.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND P.PRICE <= ?");
            parameters.add(maxPrice);
        }

        if ("PRICE_ASC".equals(sort)) {
            sql.append(" ORDER BY P.PRICE ASC NULLS LAST, P.POST_ID DESC");
        } else if ("PRICE_DESC".equals(sort)) {
            sql.append(" ORDER BY P.PRICE DESC NULLS LAST, P.POST_ID DESC");
        } else if ("FAVORITE".equals(sort)) {
            sql.append(" ORDER BY P.FAVORITE_COUNT DESC NULLS LAST, P.POST_ID DESC");
        } else {
            sql.append(" ORDER BY P.CREATED_AT DESC, P.POST_ID DESC");
        }

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapPost(rs));
                }
            }
        }

        return posts;
    }

    public int insert(Post post) throws SQLException {
        try (Connection conn = new JDBCUtil().getConnection()) {
            conn.setAutoCommit(false);

            try {
                int result = insert(conn, post);
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int insert(Connection conn, Post post) throws SQLException {
        String sequenceSql = "SELECT SEQ_POST.NEXTVAL FROM DUAL";

        try (PreparedStatement pstmt = conn.prepareStatement(sequenceSql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                post.setPostId(rs.getLong(1));
            } else {
                throw new SQLException("게시글 ID 생성 실패");
            }
        }

        String sql =
                "INSERT INTO POST "
                        + "(POST_ID, WRITER_ID, CATEGORY_ID, TITLE, SUBJECT, "
                        + "DESCRIPTION, PRICE, AVAILABLE, AVAILABLE_TIME, "
                        + "STATUS, VIEW_COUNT, FAVORITE_COUNT, CREATED_AT) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ON_SALE', 0, 0, SYSDATE)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, post.getPostId());
            pstmt.setString(2, post.getWriterId());
            pstmt.setObject(3, post.getCategoryId());
            pstmt.setString(4, post.getTitle());
            pstmt.setString(5, post.getSubject());
            pstmt.setString(6, post.getDescription());
            pstmt.setObject(7, post.getPrice());
            pstmt.setString(8, post.getAvailable());
            pstmt.setString(9, post.getAvailableTime());

            int result = pstmt.executeUpdate();

            post.setStatus("ON_SALE");
            post.setViewCount(0);
            post.setFavoriteCount(0);

            return result;
        }
    }

    public int update(Post post) throws SQLException {
        try (Connection conn = new JDBCUtil().getConnection()) {
            conn.setAutoCommit(false);

            try {
                int result = update(conn, post);
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int update(Connection conn, Post post) throws SQLException {
        String sql =
                "UPDATE POST SET CATEGORY_ID = ?, TITLE = ?, SUBJECT = ?, "
                        + "DESCRIPTION = ?, PRICE = ?, AVAILABLE = ?, "
                        + "AVAILABLE_TIME = ? "
                        + "WHERE POST_ID = ? AND WRITER_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, post.getCategoryId());
            pstmt.setString(2, post.getTitle());
            pstmt.setString(3, post.getSubject());
            pstmt.setString(4, post.getDescription());
            pstmt.setObject(5, post.getPrice());
            pstmt.setString(6, post.getAvailable());
            pstmt.setString(7, post.getAvailableTime());
            pstmt.setLong(8, post.getPostId());
            pstmt.setString(9, post.getWriterId());

            return pstmt.executeUpdate();
        }
    }

    public int updateStatus(Long postId, String writerId, String status) throws SQLException {

        if (!"ON_SALE".equals(status) && !"SOLD_OUT".equals(status)) {
            throw new IllegalArgumentException("올바르지 않은 판매 상태입니다.");
        }

        String sql = "UPDATE POST SET STATUS = ? " + "WHERE POST_ID = ? AND WRITER_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setString(1, status);
                pstmt.setLong(2, postId);
                pstmt.setString(3, writerId);

                int result = pstmt.executeUpdate();
                conn.commit();

                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int increaseViewCount(Long postId) throws SQLException {
        String sql = "UPDATE POST SET VIEW_COUNT = NVL(VIEW_COUNT, 0) + 1 " + "WHERE POST_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setLong(1, postId);

                int result = pstmt.executeUpdate();
                conn.commit();

                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int delete(Long postId, String writerId) throws SQLException {
        try (Connection conn = new JDBCUtil().getConnection()) {
            conn.setAutoCommit(false);

            try {
                int result = delete(conn, postId, writerId);
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int delete(Connection conn, Long postId, String writerId) throws SQLException {

        String sql = "DELETE FROM POST WHERE POST_ID = ? AND WRITER_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            pstmt.setString(2, writerId);

            return pstmt.executeUpdate();
        }
    }
}
