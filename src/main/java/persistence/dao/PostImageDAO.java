package persistence.dao;

import domain.PostImage;
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
        image.setImageOrder(rs.getInt("IMAGE_ORDER"));

        return image;
    }

    public List<PostImage> findByPostId(Long postId) {
        String sql =
                "SELECT IMAGE_ID, POST_ID, URL, IMAGE_ORDER "
                        + "FROM POST_IMAGE WHERE POST_ID = ? ORDER BY IMAGE_ORDER";

        JDBCUtil jdbcUtil = new JDBCUtil(sql, new Object[] {postId});
        List<PostImage> images = new ArrayList<>();

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            while (rs != null && rs.next()) {
                images.add(mapImage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            jdbcUtil.close();
        }

        return images;
    }

    public int insert(PostImage image) throws Exception {
        String sql =
                "INSERT INTO POST_IMAGE "
                        + "(IMAGE_ID, POST_ID, URL, IMAGE_ORDER) "
                        + "VALUES (?, ?, ?, ?)";

        Object[] parameters = {
            image.getImageId(), image.getPostId(), image.getUrl(), image.getImageOrder()
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

    public int update(PostImage image) throws Exception {
        String sql = "UPDATE POST_IMAGE SET URL = ?, IMAGE_ORDER = ? " + "WHERE IMAGE_ID = ?";

        Object[] parameters = {image.getUrl(), image.getImageOrder(), image.getImageId()};

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

    public int delete(Long imageId) throws Exception {
        String sql = "DELETE FROM POST_IMAGE WHERE IMAGE_ID = ?";
        JDBCUtil jdbcUtil = new JDBCUtil(sql, new Object[] {imageId});

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

    public int deleteByPostId(Long postId) throws Exception {
        String sql = "DELETE FROM POST_IMAGE WHERE POST_ID = ?";
        JDBCUtil jdbcUtil = new JDBCUtil(sql, new Object[] {postId});

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
