
package persistence.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import domain.Post;
import persistence.util.JDBCUtil;

public class PostDAO {

    private Post mapPost(ResultSet rs) throws SQLException {
        Post post = new Post();

        post.setPostId(rs.getLong("POST_ID"));
        post.setWriterId(rs.getString("WRITER_ID"));
        post.setCategoryId(rs.getObject("CATEGORY_ID") == null
        		? null : rs.getLong("CATEGORY_ID"));
        post.setTitle(rs.getString("TITLE"));
        post.setSubject(rs.getString("SUBJECT"));
        post.setDescription(rs.getString("DESCRIPTION"));
        post.setPrice(rs.getObject("PRICE") == null
        		? null : rs.getInt("PRICE"));
        post.setAvailable(rs.getString("AVAILABLE"));
        post.setAvailableTime(rs.getString("AVAILABLE_TIME"));
        post.setStatus(rs.getString("STATUS"));
        post.setViewCount(rs.getObject("VIEW_COUNT") == null
        		? null : rs.getInt("VIEW_COUNT"));
        post.setFavoriteCount(rs.getObject("FAVORITE_COUNT") == null
        		? null : rs.getInt("FAVORITE_COUNT"));

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");

        if (createdAt != null) {
            post.setCreatedAt(createdAt.toLocalDateTime());
        }

        return post;
    }

    public List<Post> findAll() {
        String sql = "SELECT * FROM POST ORDER BY CREATED_AT DESC";
        JDBCUtil jdbcUtil = new JDBCUtil(sql);
        List<Post> posts = new ArrayList<>();

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            while (rs != null && rs.next()) {
                posts.add(mapPost(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            jdbcUtil.close();
        }

        return posts;
    }

    public Post findById(Long postId) {
        String sql = "SELECT * FROM POST WHERE POST_ID = ?";
        JDBCUtil jdbcUtil = new JDBCUtil(sql, new Object[]{postId});
        Post post = null;

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            if (rs != null && rs.next()) {
                post = mapPost(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            jdbcUtil.close();
        }

        return post;
    }

    public int insert(Post post) throws Exception {
        String sql = "INSERT INTO POST "
                + "(POST_ID, SAFE_ZONE_ID, WRITER_ID, CATEGORY_ID, TITLE, SUBJECT, "
                + "DESCRIPTION, PRICE, AVAILABLE, AVAILABLE_TIME, STATUS, "
                + "VIEW_COUNT, FAVORITE_COUNT, CREATED_AT, TRADE_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?)";

        Object[] parameters = {
                post.getPostId(),
                post.getWriterId(),
                post.getCategoryId(),
                post.getTitle(),
                post.getSubject(),
                post.getDescription(),
                post.getPrice(),
                post.getAvailable(),
                post.getAvailableTime(),
                post.getStatus(),
                post.getViewCount(),
                post.getFavoriteCount()
        };

        JDBCUtil jdbcUtil = new JDBCUtil(sql, parameters);

        try {
            int result = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return result;
        } catch (Exception e) {
            jdbcUtil.rollback();
            throw e;
        } finally {
            jdbcUtil.close();
        }
    }

    public int update(Post post) throws Exception {
        String sql = "UPDATE POST SET "
                + "CATEGORY_ID = ?, TITLE = ?, SUBJECT = ?, DESCRIPTION = ?, "
                + "PRICE = ?, AVAILABLE = ?, AVAILABLE_TIME = ?, STATUS = ? "
                + "WHERE POST_ID = ?";

        Object[] parameters = {
                post.getCategoryId(),
                post.getTitle(),
                post.getSubject(),
                post.getDescription(),
                post.getPrice(),
                post.getAvailable(),
                post.getAvailableTime(),
                post.getStatus(),
                post.getPostId()
        };

        JDBCUtil jdbcUtil = new JDBCUtil(sql, parameters);

        try {
            int result = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return result;
        } catch (Exception e) {
            jdbcUtil.rollback();
            throw e;
        } finally {
            jdbcUtil.close();
        }
    }

    public int updateStatus(Long postId, String status) throws Exception {
        String sql = "UPDATE POST SET STATUS = ? WHERE POST_ID = ?";
        JDBCUtil jdbcUtil = new JDBCUtil(sql, new Object[]{status, postId});

        try {
            int result = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return result;
        } catch (Exception e) {
            jdbcUtil.rollback();
            throw e;
        } finally {
            jdbcUtil.close();
        }
    }

    public int delete(Long postId) throws Exception {
        String sql = "DELETE FROM POST WHERE POST_ID = ?";
        JDBCUtil jdbcUtil = new JDBCUtil(sql, new Object[]{postId});

        try {
            int result = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return result;
        } catch (Exception e) {
            jdbcUtil.rollback();
            throw e;
        } finally {
            jdbcUtil.close();
        }
    }
}
