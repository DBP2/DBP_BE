package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import domain.Post;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class PostDAOTest {

    private final PostDAO postDAO = new PostDAO();

    @Test
    void findAll() throws Exception {
        List<Post> posts = postDAO.findAll();

        assertNotNull(posts);

        System.out.println("전체 게시글 조회 성공");
        System.out.println("전체 게시글 개수: " + posts.size());
    }

    @Test
    void findOnSale() throws Exception {
        List<Post> posts = postDAO.findOnSale();

        assertNotNull(posts);

        System.out.println("판매 중인 게시글 조회 성공");
        System.out.println("판매 중인 게시글 개수: " + posts.size());
    }

    @Test
    void search() throws Exception {
        List<Post> posts = postDAO.search(null, null, null, null, null, "LATEST");

        assertNotNull(posts);

        System.out.println("게시글 검색 성공");
        System.out.println("검색 결과 개수: " + posts.size());
    }
}
