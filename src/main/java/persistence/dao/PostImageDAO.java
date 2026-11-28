package persistence.dao;

import domain.PostImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;

public class PostImageDAO {

    private PostImage mapImage(ResultSet rs) throws SQLException {
        PostImage image = new PostImage();

        image.setImageId(rs.getLong("IMAGE_ID"));
        image.setPostId(rs.getLong("POST_ID"));
        image.setUrl(rs.getString("URL"));
        image.setImageOrder(rs.getObject("IMAGE_ORDER") == null ? null : rs.getInt("IMAGE_ORDER"));

        return image;
    }

    public List<PostImage> findByPostId(Long postId) throws SQLException {
        try (Connection conn = new JDBCUtil().getConnection()) {
            return findByPostId(conn, postId);
        }
    }

    public List<PostImage> findByPostId(Connection conn, Long postId) throws SQLException {

        String sql =
                "SELECT IMAGE_ID, POST_ID, URL, IMAGE_ORDER "
                        + "FROM POST_IMAGE WHERE POST_ID = ? "
                        + "ORDER BY IMAGE_ORDER, IMAGE_ID";

        List<PostImage> images = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    images.add(mapImage(rs));
                }
            }
        }

        return images;
    }

    public int insert(PostImage image) throws SQLException {
        try (Connection conn = new JDBCUtil().getConnection()) {
            conn.setAutoCommit(false);

            try {
                int result = insert(conn, image);
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int insert(Connection conn, PostImage image) throws SQLException {
        String sequenceSql = "SELECT SEQ_POST_IMAGE.NEXTVAL FROM DUAL";

        try (PreparedStatement pstmt = conn.prepareStatement(sequenceSql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                image.setImageId(rs.getLong(1));
            } else {
                throw new SQLException("이미지 ID 생성 실패");
            }
        }

        String sql =
                "INSERT INTO POST_IMAGE "
                        + "(IMAGE_ID, POST_ID, URL, IMAGE_ORDER) "
                        + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, image.getImageId());
            pstmt.setLong(2, image.getPostId());
            pstmt.setString(3, image.getUrl());
            pstmt.setObject(4, image.getImageOrder());

            return pstmt.executeUpdate();
        }
    }

    public int insertAll(Connection conn, Long postId, List<PostImage> images) throws SQLException {

        if (images == null || images.isEmpty() || images.size() > 10) {
            throw new IllegalArgumentException("게시글 이미지는 최소 1장, 최대 10장까지 등록할 수 있습니다.");
        }

        int result = 0;

        for (int i = 0; i < images.size(); i++) {
            PostImage image = images.get(i);

            image.setPostId(postId);
            image.setImageOrder(i + 1);

            result += insert(conn, image);
        }

        return result;
    }

    public int update(PostImage image) throws SQLException {
        String sql =
                "UPDATE POST_IMAGE SET URL = ?, IMAGE_ORDER = ? "
                        + "WHERE IMAGE_ID = ? AND POST_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setString(1, image.getUrl());
                pstmt.setObject(2, image.getImageOrder());
                pstmt.setLong(3, image.getImageId());
                pstmt.setLong(4, image.getPostId());

                int result = pstmt.executeUpdate();
                conn.commit();

                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int delete(Long imageId) throws SQLException {
        String sql = "DELETE FROM POST_IMAGE WHERE IMAGE_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setLong(1, imageId);

                int result = pstmt.executeUpdate();
                conn.commit();

                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int deleteByPostId(Long postId) throws SQLException {
        try (Connection conn = new JDBCUtil().getConnection()) {
            conn.setAutoCommit(false);

            try {
                int result = deleteByPostId(conn, postId);
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int deleteByPostId(Connection conn, Long postId) throws SQLException {

        String sql = "DELETE FROM POST_IMAGE WHERE POST_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);

            return pstmt.executeUpdate();
        }
    }
}
