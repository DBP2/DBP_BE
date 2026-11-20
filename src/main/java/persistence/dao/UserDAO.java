package persistence.dao;

import domain.User;

import persistence.util.JDBCUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** MEMBER (+ STUDENT · SCHOOL 조인). 회원가입 · 로그인 · 내 프로필 */
public class UserDAO {

    private JDBCUtil jdbcUtil = null;

    public UserDAO() {
        jdbcUtil = new JDBCUtil();
    }

    private static String query =
            "SELECT m.USER_ID, m.PASSWORD, m.NICKNAME, m.PROFILE_IMAGE, m.INTRODUCTION,"
                + " m.TRUST_SCORE, m.MEMBER_ROLE, m.MEMBER_STATUS, m.CREATED_AT, s.STUDENT_ID,"
                + " s.STUDENT_NO, s.STUDENT_NAME, s.DEPARTMENT, s.EMAIL, s.ENROLL_STATUS,"
                + " sc.SCHOOL_ID, sc.SCHOOL_NAME, sc.EMAIL_DOMAIN FROM MEMBER m LEFT JOIN STUDENT s"
                + " ON m.STUDENT_ID = s.STUDENT_ID LEFT JOIN SCHOOL sc ON s.SCHOOL_ID ="
                + " sc.SCHOOL_ID ";

    /** 회원 등록 (가입). TRUST_SCORE · MEMBER_STATUS · CREATED_AT 은 DB 기본값 사용 */
    public int create(User user) {
        String sql =
                "INSERT INTO MEMBER (USER_ID, PASSWORD, NICKNAME, PROFILE_IMAGE, INTRODUCTION, "
                        + "MEMBER_ROLE, STUDENT_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Object[] param =
                new Object[] {
                    user.getUserId(),
                    user.getPassword(),
                    user.getNickname(),
                    user.getProfileImage(),
                    user.getIntroduction(),
                    user.getMemberRole(),
                    user.getStudent() == null ? null : user.getStudent().getStudentId()
                };
        return executeUpdate(sql, param);
    }

    /** 회원 1명 조회 (학적·학교 포함). 없으면 null */
    public User findUser(String userId) {
        jdbcUtil.setSqlAndParameters(query + "WHERE m.USER_ID = ?", new Object[] {userId});
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                return toUser(rs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    /** 아이디 중복 확인 */
    public boolean existsUserId(String userId) {
        return exists("SELECT COUNT(*) AS CNT FROM MEMBER WHERE USER_ID = ?", userId);
    }

    /** 닉네임 중복 확인 */
    public boolean existsNickname(String nickname) {
        return exists("SELECT COUNT(*) AS CNT FROM MEMBER WHERE NICKNAME = ?", nickname);
    }

    /** 이 학적으로 이미 가입한 회원이 있는지 (학생 1명당 계정 1개) */
    public boolean existsStudent(Long studentId) {
        return exists("SELECT COUNT(*) AS CNT FROM MEMBER WHERE STUDENT_ID = ?", studentId);
    }

    /** 프로필 수정 (닉네임·사진·소개) */
    public int update(User user) {
        String sql =
                "UPDATE MEMBER SET NICKNAME = ?, PROFILE_IMAGE = ?, INTRODUCTION = ? WHERE USER_ID"
                        + " = ?";
        return executeUpdate(
                sql,
                new Object[] {
                    user.getNickname(),
                    user.getProfileImage(),
                    user.getIntroduction(),
                    user.getUserId()
                });
    }

    /** 비밀번호 변경 */
    public int updatePassword(String userId, String newPassword) {
        return executeUpdate(
                "UPDATE MEMBER SET PASSWORD = ? WHERE USER_ID = ?",
                new Object[] {newPassword, userId});
    }

    /** 회원 상태 변경 (ACTIVE / SUSPENDED / BANNED / WITHDRAWN). 탈퇴도 이것으로 처리 */
    public int updateStatus(String userId, String status) {
        return executeUpdate(
                "UPDATE MEMBER SET MEMBER_STATUS = ? WHERE USER_ID = ?",
                new Object[] {status, userId});
    }

    /** 신뢰 점수 증감 (delta 가 음수면 감소). 리뷰·제재 처리에서 호출 */
    public int updateTrustScore(String userId, int delta) {
        return executeUpdate(
                "UPDATE MEMBER SET TRUST_SCORE = TRUST_SCORE + ? WHERE USER_ID = ?",
                new Object[] {delta, userId});
    }

    private boolean exists(String sql, Object value) {
        jdbcUtil.setSqlAndParameters(sql, new Object[] {value});
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                return rs.getInt("CNT") > 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return false;
    }

    private int executeUpdate(String sql, Object[] param) {
        jdbcUtil.setSqlAndParameters(sql, param);
        try {
            int result = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return result;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return 0;
    }

    /** ResultSet 의 현재 행 → User (학적이 없으면 student = null) */
    private static User toUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getString("USER_ID"));
        u.setPassword(rs.getString("PASSWORD"));
        u.setNickname(rs.getString("NICKNAME"));
        u.setProfileImage(rs.getString("PROFILE_IMAGE"));
        u.setIntroduction(rs.getString("INTRODUCTION"));
        u.setTrustScore(rs.getInt("TRUST_SCORE"));
        u.setMemberRole(rs.getString("MEMBER_ROLE"));
        u.setMemberStatus(rs.getString("MEMBER_STATUS"));
        Timestamp created = rs.getTimestamp("CREATED_AT");
        u.setCreatedAt(created == null ? null : created.toLocalDateTime());
        if (rs.getObject("STUDENT_ID") != null) {
            u.setStudent(StudentDAO.toStudent(rs));
        }
        return u;
    }
}
