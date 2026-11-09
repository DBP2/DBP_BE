package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.Student;
import domain.User;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class UserDAOTest {

    private final UserDAO dao = new UserDAO();

    @BeforeAll
    static void setUp() {
        MemberTestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        MemberTestFixture.tearDown();
    }

    @AfterEach
    void removeNewUser() {
        MemberTestFixture.execute(
                "DELETE FROM MEMBER WHERE USER_ID = ?",
                new Object[] {MemberTestFixture.NEW_USER_ID});
        MemberTestFixture.execute(
                "UPDATE MEMBER SET NICKNAME = '테스트회원', PASSWORD = 'pw1234', MEMBER_STATUS ="
                        + " 'ACTIVE', TRUST_SCORE = 12, INTRODUCTION = NULL WHERE USER_ID = ?",
                new Object[] {MemberTestFixture.USER_ID});
    }

    @Test
    void create() {
        Student student = new Student();
        student.setStudentId(MemberTestFixture.FREE_STUDENT_ID);
        User user = new User(MemberTestFixture.NEW_USER_ID, "newpw", "새회원테스트", student);

        assertEquals(1, dao.create(user));

        User saved = dao.findUser(MemberTestFixture.NEW_USER_ID);
        assertNotNull(saved);
        assertEquals(User.ROLE_USER, saved.getMemberRole()); // DB 기본값 'USER'
        assertEquals(User.ACTIVE, saved.getMemberStatus());
        assertEquals(0, saved.getTrustScore());
        assertEquals("C", saved.getTrustGrade());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findUser() {
        User user = dao.findUser(MemberTestFixture.USER_ID);

        assertNotNull(user);
        assertEquals("테스트회원", user.getNickname());
        assertTrue(user.isSchoolVerified());
        assertEquals("테스트대학교", user.getStudent().getSchool().getSchoolName());
        assertEquals("B", user.getTrustGrade());
    }

    @Test
    void findAdminWithoutStudent() {
        User admin = dao.findUser(MemberTestFixture.ADMIN_ID);

        assertNotNull(admin);
        assertTrue(admin.isAdmin());
        assertNull(admin.getStudent());
    }

    @Test
    void findUserNotFound() {
        assertNull(dao.findUser("no_such_user"));
    }

    @Test
    void exists() {
        assertTrue(dao.existsUserId(MemberTestFixture.USER_ID));
        assertFalse(dao.existsUserId("no_such_user"));
        assertTrue(dao.existsNickname("테스트회원"));
        assertTrue(dao.existsStudent(MemberTestFixture.STUDENT_ID));
        assertFalse(dao.existsStudent(MemberTestFixture.FREE_STUDENT_ID));
    }

    @Test
    void update() {
        User user = dao.findUser(MemberTestFixture.USER_ID);
        user.setNickname("바뀐닉네임");
        user.setIntroduction("소개 테스트");

        assertEquals(1, dao.update(user));
        assertEquals("바뀐닉네임", dao.findUser(MemberTestFixture.USER_ID).getNickname());
    }

    @Test
    void updatePasswordStatusTrust() {
        assertEquals(1, dao.updatePassword(MemberTestFixture.USER_ID, "changed"));
        assertEquals(1, dao.updateStatus(MemberTestFixture.USER_ID, User.WITHDRAWN));
        assertEquals(1, dao.updateTrustScore(MemberTestFixture.USER_ID, 20));

        User user = dao.findUser(MemberTestFixture.USER_ID);
        assertTrue(user.matchPassword("changed"));
        assertEquals(User.WITHDRAWN, user.getMemberStatus());
        assertEquals(32, user.getTrustScore());
        assertEquals("A", user.getTrustGrade());
    }
}
