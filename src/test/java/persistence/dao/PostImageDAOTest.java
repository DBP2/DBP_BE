package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import domain.PostImage;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class PostImageDAOTest {

    private final PostImageDAO postImageDAO = new PostImageDAO();

    @Test
    void findByPostId() throws Exception {
        List<PostImage> images = postImageDAO.findByPostId(1L);

        assertNotNull(images);

        System.out.println("게시글 이미지 조회 성공");
        System.out.println("이미지 개수: " + images.size());
    }
}
