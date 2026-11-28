package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import domain.Category;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class CategoryDAOTest {

    @Test
    void findAll() throws Exception {
        CategoryDAO categoryDAO = new CategoryDAO();

        List<Category> categories = categoryDAO.findAll();

        assertNotNull(categories);

        System.out.println("DB 연결 성공");
        System.out.println("카테고리 개수: " + categories.size());
    }
}
