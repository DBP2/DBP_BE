package persistence.dao;

import domain.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;

public class CategoryDAO {

    private JDBCUtil jdbcUtil = null;

    public CategoryDAO() {
        jdbcUtil = new JDBCUtil();
    }

    public List<Category> findAll() {
        String sql =
                "SELECT CATEGORY_ID, NAME, COLLEGE, GRADE, CLASSIFICATION FROM CATEGORY ORDER BY CATEGORY_ID";

        jdbcUtil.setSql(sql);
        ResultSet rs = null;
        List<Category> categories = new ArrayList<>();

        try {
            rs = jdbcUtil.executeQuery();

            while (rs != null && rs.next()) {
                Category category = new Category();

                category.setCategoryId(rs.getLong("CATEGORY_ID"));
                category.setName(rs.getString("NAME"));
                category.setCollege(rs.getString("COLLEGE"));
                category.setGrade(rs.getInt("GRADE"));
                category.setClassification(rs.getString("CLASSIFICATION"));

                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            jdbcUtil.close();
        }

        return categories;
    }

    public Category findById(Long categoryId) {
        String sql =
                "SELECT CATEGORY_ID, NAME, COLLEGE, GRADE, CLASSIFICATION FROM CATEGORY WHERE CATEGORY_ID = ?";

        jdbcUtil.setSqlAndParameters(sql, new Object[] {categoryId});

        ResultSet rs = null;
        Category category = null;

        try {
            rs = jdbcUtil.executeQuery();

            if (rs != null && rs.next()) {
                category = new Category();

                category.setCategoryId(rs.getLong("CATEGORY_ID"));
                category.setName(rs.getString("NAME"));
                category.setCollege(rs.getString("COLLEGE"));
                category.setGrade(rs.getInt("GRADE"));
                category.setClassification(rs.getString("CLASSIFICATION"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            jdbcUtil.close();
        }

        return category;
    }

    public int insert(Category category) throws Exception {
        String sql =
                "INSERT INTO CATEGORY "
                        + "(CATEGORY_ID, NAME, COLLEGE, GRADE, CLASSIFICATION) "
                        + "VALUES (?, ?, ?, ?, ?)";

        Object[] parameters = {
            category.getCategoryId(),
            category.getName(),
            category.getCollege(),
            category.getGrade(),
            category.getClassification()
        };

        JDBCUtil util = new JDBCUtil(sql, parameters);

        try {
            int result = util.executeUpdate();
            util.commit();
            return result;
        } catch (Exception e) {
            util.rollback();
            throw e;
        } finally {
            util.close();
        }
    }

    public int update(Category category) throws Exception {
        String sql =
                "UPDATE CATEGORY SET "
                        + "NAME = ?, COLLEGE = ?, GRADE = ?, CLASSIFICATION = ? "
                        + "WHERE CATEGORY_ID = ?";

        Object[] parameters = {
            category.getName(),
            category.getCollege(),
            category.getGrade(),
            category.getClassification(),
            category.getCategoryId()
        };

        JDBCUtil util = new JDBCUtil(sql, parameters);

        try {
            int result = util.executeUpdate();
            util.commit();
            return result;
        } catch (Exception e) {
            util.rollback();
            throw e;
        } finally {
            util.close();
        }
    }

    public int delete(Long categoryId) throws Exception {
        String sql = "DELETE FROM CATEGORY WHERE CATEGORY_ID = ?";
        JDBCUtil util = new JDBCUtil(sql, new Object[] {categoryId});

        try {
            int result = util.executeUpdate();
            util.commit();
            return result;
        } catch (Exception e) {
            util.rollback();
            throw e;
        } finally {
            util.close();
        }
    }
}
