package persistence.dao;

import domain.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;

public class CategoryDAO {

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category category = new Category();

        category.setCategoryId(rs.getLong("CATEGORY_ID"));
        category.setName(rs.getString("NAME"));
        category.setCollege(rs.getString("COLLEGE"));
        category.setGrade(rs.getObject("GRADE") == null ? null : rs.getInt("GRADE"));
        category.setClassification(rs.getString("CLASSIFICATION"));

        return category;
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT * FROM CATEGORY ORDER BY CATEGORY_ID";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
        }

        return categories;
    }

    public Category findById(Long categoryId) throws SQLException {
        String sql = "SELECT * FROM CATEGORY WHERE CATEGORY_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }
        }

        return null;
    }

    public int insert(Category category) throws SQLException {
        String sql =
                "INSERT INTO CATEGORY "
                        + "(CATEGORY_ID, NAME, COLLEGE, GRADE, CLASSIFICATION) "
                        + "VALUES (SEQ_CATEGORY.NEXTVAL, ?, ?, ?, ?)";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setString(1, category.getName());
                pstmt.setString(2, category.getCollege());
                pstmt.setObject(3, category.getGrade());
                pstmt.setString(4, category.getClassification());

                int result = pstmt.executeUpdate();
                conn.commit();

                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int update(Category category) throws SQLException {
        String sql =
                "UPDATE CATEGORY SET "
                        + "NAME = ?, COLLEGE = ?, GRADE = ?, CLASSIFICATION = ? "
                        + "WHERE CATEGORY_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setString(1, category.getName());
                pstmt.setString(2, category.getCollege());
                pstmt.setObject(3, category.getGrade());
                pstmt.setString(4, category.getClassification());
                pstmt.setLong(5, category.getCategoryId());

                int result = pstmt.executeUpdate();
                conn.commit();

                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int delete(Long categoryId) throws SQLException {
        String sql = "DELETE FROM CATEGORY WHERE CATEGORY_ID = ?";

        try (Connection conn = new JDBCUtil().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                pstmt.setLong(1, categoryId);

                int result = pstmt.executeUpdate();
                conn.commit();

                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
