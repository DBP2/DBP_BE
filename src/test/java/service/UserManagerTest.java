package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import persistence.dao.MemberTestFixture;
import service.exception.BadRequestException;
import service.exception.ConflictException;
import service.exception.ForbiddenException;
import service.exception.NotFoundException;
import service.exception.UnauthorizedException;

@Tag("db")
class UserManagerTest {
    private final UserManager userMan = UserManager.getInstance();

    @BeforeAll
    static void setUp() {
        MemberTestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        MemberTestFixture.tearDown();
    }

    @BeforeEach
    void reset() {
        MemberTestFixture.resetSanctions();
        MemberTestFixture.execute(
                "DELETE FROM MEMBER WHERE USER_ID = ?",
                new Object[] {MemberTestFixture.NEW_USER_ID});
    }

    @Test
    void signUpWithEnrolledStudent() throws Exception {
        User user =
                userMan.signUp(
                        MemberTestFixture.SCHOOL_ID,
                        "T900004",
                        "미가입학생",
                        MemberTestFixture.NEW_USER_ID,
                        "pw1234",
                        "새회원닉네임");

        assertEquals(MemberTestFixture.NEW_USER_ID, user.getUserId());
        assertTrue(user.isSchoolVerified());
        assertFalse(userMan.isUserIdAvailable(MemberTestFixture.NEW_USER_ID));
    }

    @Test
    void signUpWithWrongName() {
        assertThrows(
                NotFoundException.class,
                () ->
                        userMan.signUp(
                                MemberTestFixture.SCHOOL_ID,
                                "T900004",
                                "다른이름",
                                MemberTestFixture.NEW_USER_ID,
                                "pw1234",
                                "새회원닉네임"));
    }

    @Test
    void signUpWithLeaveStudent() {
        assertThrows(
                BadRequestException.class,
                () ->
                        userMan.signUp(
                                MemberTestFixture.SCHOOL_ID,
                                "T900002",
                                "휴학학생",
                                MemberTestFixture.NEW_USER_ID,
                                "pw1234",
                                "새회원닉네임"));
    }

    @Test
    void signUpWithUsedStudent() {
        assertThrows(
                ConflictException.class,
                () ->
                        userMan.signUp(
                                MemberTestFixture.SCHOOL_ID,
                                MemberTestFixture.STUDENT_NO,
                                "테스트학생",
                                MemberTestFixture.NEW_USER_ID,
                                "pw1234",
                                "새회원닉네임"));
    }

    @Test
    void signUpWithBadUserId() {
        assertThrows(
                BadRequestException.class,
                () ->
                        userMan.signUp(
                                MemberTestFixture.SCHOOL_ID,
                                "T900004",
                                "미가입학생",
                                "Bad Id!",
                                "pw1234",
                                "새회원닉네임"));
    }

    @Test
    void loginAndWrongPassword() throws Exception {
        User user = userMan.login(MemberTestFixture.USER_ID, "pw1234");

        assertEquals("테스트회원", user.getNickname());
        assertThrows(
                UnauthorizedException.class,
                () -> userMan.login(MemberTestFixture.USER_ID, "wrong"));
    }

    @Test
    void loginSuspendedUser() {
        MemberTestFixture.execute(
                "INSERT INTO SANCTION (SANCTION_ID, TARGET_ID, ADMIN_ID, SANCTION_TYPE, REASON,"
                        + " END_AT) VALUES (SEQ_SANCTION.NEXTVAL, ?, ?, 'SUSPENSION', '테스트', SYSDATE"
                        + " + 3)",
                new Object[] {MemberTestFixture.USER_ID, MemberTestFixture.ADMIN_ID});
        MemberTestFixture.execute(
                "UPDATE MEMBER SET MEMBER_STATUS = 'SUSPENDED' WHERE USER_ID = ?",
                new Object[] {MemberTestFixture.USER_ID});

        assertThrows(
                ForbiddenException.class, () -> userMan.login(MemberTestFixture.USER_ID, "pw1234"));
    }

    @Test
    void checkNicknameAvailable() throws Exception {
        assertFalse(userMan.isNicknameAvailable("테스트회원"));
        assertTrue(userMan.isNicknameAvailable("아무도안쓰는닉"));
    }

    @Test
    void updateProfileAndPassword() throws Exception {
        User user = userMan.updateProfile(MemberTestFixture.USER_ID, null, "안녕하세요");
        assertEquals("안녕하세요", user.getIntroduction());

        assertThrows(
                ConflictException.class,
                () -> userMan.updateProfile(MemberTestFixture.USER_ID, "테스트관리자", null));

        userMan.changePassword(MemberTestFixture.USER_ID, "pw1234", "newpw99");
        userMan.login(MemberTestFixture.USER_ID, "newpw99");
        userMan.changePassword(MemberTestFixture.USER_ID, "newpw99", "pw1234");
    }
}
