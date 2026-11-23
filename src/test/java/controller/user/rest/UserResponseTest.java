package controller.user.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import domain.School;
import domain.Student;
import domain.User;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserResponseTest {

    @Test
    void meMapUsesNicknameAsName() {
        Student student = new Student();
        student.setSchool(new School(1L, "동덕여자대학교", "dongduk.ac.kr"));
        User user = new User("minjun01", "secret", "민준", student);
        user.setTrustScore(12);

        Map<String, Object> map = UserResponse.toMeMap(user);

        assertEquals("minjun01", map.get("id"));
        assertEquals("민준", map.get("name"));
        assertEquals("B", map.get("trustGrade"));
        assertEquals("동덕여자대학교", map.get("schoolName"));
        assertFalse(map.containsKey("password"));
    }

    @Test
    void profileMapWithoutSchool() {
        User admin = new User("admin", "secret", "관리자", null);

        Map<String, Object> map = UserResponse.toProfileMap(admin);

        assertEquals(false, map.get("schoolVerified"));
        assertNull(map.get("schoolName"));
        assertFalse(map.containsKey("admin"));
    }

    @Test
    void toLongFromNumberOrString() {
        assertEquals(3L, UserResponse.toLong(3));
        assertEquals(3L, UserResponse.toLong("3"));
        assertNull(UserResponse.toLong("abc"));
    }
}
