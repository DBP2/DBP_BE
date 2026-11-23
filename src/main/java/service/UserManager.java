package service;

import domain.Sanction;
import domain.School;
import domain.Student;
import domain.User;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import persistence.dao.SanctionDAO;
import persistence.dao.StudentDAO;
import persistence.dao.UserDAO;
import service.exception.BadRequestException;
import service.exception.ConflictException;
import service.exception.ForbiddenException;
import service.exception.NotFoundException;
import service.exception.UnauthorizedException;

/** 회원가입(학적 확인)·로그인·내 프로필 */
public class UserManager {
    public static final int MAX_USER_ID_BYTES = 20;
    public static final int MIN_USER_ID_LENGTH = 4;
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int MAX_PASSWORD_BYTES = 100;
    public static final int MAX_NICKNAME_BYTES = 30;
    public static final int MAX_INTRODUCTION_BYTES = 300;

    private static UserManager userMan = new UserManager();
    private UserDAO userDAO;
    private StudentDAO studentDAO;
    private SanctionDAO sanctionDAO;

    private UserManager() {
        try {
            userDAO = new UserDAO();
            studentDAO = new StudentDAO();
            sanctionDAO = new SanctionDAO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UserManager getInstance() {
        return userMan;
    }

    /** 가입 화면의 학교 선택 목록 */
    public List<School> findSchoolList() throws SQLException {
        List<School> schoolList = studentDAO.findSchoolList();
        if (schoolList == null) {
            throw new SQLException("학교 목록을 불러오지 못했어요");
        }
        return schoolList;
    }

    /** 아이디를 쓸 수 있는지 (형식이 맞고 아직 아무도 안 씀) */
    public boolean isUserIdAvailable(String userId) throws BadRequestException {
        checkUserId(userId);
        return !userDAO.existsUserId(userId);
    }

    /** 닉네임을 쓸 수 있는지 */
    public boolean isNicknameAvailable(String nickname) throws BadRequestException {
        checkNickname(nickname);
        return !userDAO.existsNickname(nickname.trim());
    }

    /**
     * 회원가입. 학교+학번+이름으로 더미 학적을 확인하고, 재학생이며 아직 가입하지 않은 학적이어야 가입된다.
     *
     * @return 가입된 회원
     */
    public User signUp(
            Long schoolId,
            String studentNo,
            String studentName,
            String userId,
            String password,
            String nickname)
            throws SQLException, BadRequestException, NotFoundException, ConflictException {
        checkUserId(userId);
        checkPassword(password);
        checkNickname(nickname);
        if (schoolId == null || isBlank(studentNo) || isBlank(studentName)) {
            throw new BadRequestException("학교, 학번, 이름을 모두 입력해 주세요");
        }

        Student student = studentDAO.findStudent(schoolId, studentNo.trim());
        if (student == null || !student.getStudentName().equals(studentName.trim())) {
            throw new NotFoundException("학적 정보를 찾을 수 없어요. 학교, 학번, 이름을 확인해 주세요");
        }
        if (!student.isEnrolled()) {
            throw new BadRequestException("재학 중인 학생만 가입할 수 있어요");
        }
        if (userDAO.existsStudent(student.getStudentId())) {
            throw new ConflictException("이미 가입한 학적이에요");
        }
        if (userDAO.existsUserId(userId)) {
            throw new ConflictException("이미 사용 중인 아이디예요");
        }
        if (userDAO.existsNickname(nickname.trim())) {
            throw new ConflictException("이미 사용 중인 닉네임이에요");
        }

        User user = new User(userId, password, nickname.trim(), student);
        if (userDAO.create(user) != 1) {
            throw new SQLException("회원가입에 실패했어요");
        }
        return userDAO.findUser(userId);
    }

    /**
     * 로그인. 기간이 끝난 정지는 먼저 풀어 주고, 아직 정지 중이면 로그인을 막는다.
     *
     * @return 로그인한 회원
     */
    public User login(String userId, String password)
            throws UnauthorizedException, ForbiddenException {
        if (isBlank(userId) || isBlank(password)) {
            throw new UnauthorizedException("아이디와 비밀번호를 입력해 주세요");
        }
        sanctionDAO.refreshStatus(userId);

        User user = userDAO.findUser(userId);
        if (user == null
                || User.WITHDRAWN.equals(user.getMemberStatus())
                || !user.matchPassword(password)) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 맞지 않아요");
        }
        if (User.BANNED.equals(user.getMemberStatus())) {
            throw new ForbiddenException("이용이 영구 정지된 계정이에요");
        }
        if (User.SUSPENDED.equals(user.getMemberStatus())) {
            Sanction sanction = sanctionDAO.findActiveByUser(userId);
            String days = sanction == null ? "" : " (남은 기간 " + sanction.getRemainingDays() + "일)";
            throw new ForbiddenException("이용이 정지된 계정이에요" + days);
        }
        return user;
    }

    /** 회원 1명 조회 (학적·학교 포함) */
    public User findUser(String userId) throws NotFoundException {
        User user = userDAO.findUser(userId);
        if (user == null || User.WITHDRAWN.equals(user.getMemberStatus())) {
            throw new NotFoundException("회원을 찾을 수 없어요");
        }
        return user;
    }

    /** 내 프로필 수정 (닉네임·소개). null 로 보낸 항목은 바꾸지 않는다 */
    public User updateProfile(String userId, String nickname, String introduction)
            throws SQLException, BadRequestException, NotFoundException, ConflictException {
        User user = findUser(userId);

        if (nickname != null && !nickname.trim().equals(user.getNickname())) {
            checkNickname(nickname);
            if (userDAO.existsNickname(nickname.trim())) {
                throw new ConflictException("이미 사용 중인 닉네임이에요");
            }
            user.setNickname(nickname.trim());
        }
        if (introduction != null) {
            if (bytes(introduction) > MAX_INTRODUCTION_BYTES) {
                throw new BadRequestException("소개는 한글 100자 이내로 써 주세요");
            }
            user.setIntroduction(introduction.isBlank() ? null : introduction.trim());
        }

        if (userDAO.update(user) != 1) {
            throw new SQLException("프로필을 수정하지 못했어요");
        }
        return user;
    }

    /** 비밀번호 변경. 현재 비밀번호가 맞아야 바뀐다 */
    public void changePassword(String userId, String currentPassword, String newPassword)
            throws SQLException, BadRequestException, NotFoundException, UnauthorizedException {
        User user = findUser(userId);
        if (!user.matchPassword(currentPassword)) {
            throw new UnauthorizedException("현재 비밀번호가 맞지 않아요");
        }
        checkPassword(newPassword);
        if (userDAO.updatePassword(userId, newPassword) != 1) {
            throw new SQLException("비밀번호를 바꾸지 못했어요");
        }
    }

    private void checkUserId(String userId) throws BadRequestException {
        if (userId == null
                || !userId.matches("[a-z0-9_]+")
                || userId.length() < MIN_USER_ID_LENGTH
                || userId.length() > MAX_USER_ID_BYTES) {
            throw new BadRequestException("아이디는 영어 소문자, 숫자, _ 로 4~20자여야 해요");
        }
    }

    private void checkPassword(String password) throws BadRequestException {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || bytes(password) > MAX_PASSWORD_BYTES) {
            throw new BadRequestException("비밀번호는 4자 이상이어야 해요");
        }
    }

    private void checkNickname(String nickname) throws BadRequestException {
        if (isBlank(nickname)) {
            throw new BadRequestException("닉네임을 입력해 주세요");
        }
        if (bytes(nickname.trim()) > MAX_NICKNAME_BYTES) {
            throw new BadRequestException("닉네임은 한글 10자(영어 30자) 이내로 써 주세요");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static int bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8).length;
    }
}
