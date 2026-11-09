package persistence.dao;

import persistence.util.JDBCUtil;

/** 회원·학적·제재 DAO 테스트용 데이터. 실제 예제 데이터와 겹치지 않게 900000번대 ID 와 test_mem_ 아이디를 쓴다. */
class MemberTestFixture {

    static final Long SCHOOL_ID = 99001L; // SCHOOL_ID 는 NUMBER(5) 라 99999 이하
    static final Long STUDENT_ID = 900001L; // USER_ID 와 연결된 학적
    static final Long LEAVE_STUDENT_ID = 900002L; // 휴학생 (가입 불가 확인용)
    static final Long REPORTER_STUDENT_ID = 900003L;
    static final Long FREE_STUDENT_ID = 900004L; // 아직 가입 안 한 학적 (create 테스트용)
    static final String STUDENT_NO = "T900001";
    static final String USER_ID = "test_mem_user";
    static final String ADMIN_ID = "test_mem_admin";
    static final String REPORTER_ID = "test_mem_reporter";
    static final String NEW_USER_ID = "test_mem_new";
    static final Long REPORT_ID = 900001L;

    static void setUp() {
        tearDown();
        execute(
                "INSERT INTO SCHOOL (SCHOOL_ID, SCHOOL_NAME, EMAIL_DOMAIN) VALUES (?, ?, ?)",
                new Object[] {SCHOOL_ID, "테스트대학교", "test900001.ac.kr"});
        insertStudent(STUDENT_ID, STUDENT_NO, "테스트학생", "ENROLLED");
        insertStudent(LEAVE_STUDENT_ID, "T900002", "휴학학생", "LEAVE");
        insertStudent(REPORTER_STUDENT_ID, "T900003", "신고학생", "ENROLLED");
        insertStudent(FREE_STUDENT_ID, "T900004", "미가입학생", "ENROLLED");
        execute(
                "INSERT INTO MEMBER (USER_ID, PASSWORD, NICKNAME, TRUST_SCORE, STUDENT_ID) "
                        + "VALUES (?, ?, ?, ?, ?)",
                new Object[] {USER_ID, "pw1234", "테스트회원", 12, STUDENT_ID});
        execute(
                "INSERT INTO MEMBER (USER_ID, PASSWORD, NICKNAME, MEMBER_ROLE) VALUES (?, ?, ?,"
                        + " 'ADMIN')",
                new Object[] {ADMIN_ID, "pw1234", "테스트관리자"});
        execute(
                "INSERT INTO MEMBER (USER_ID, PASSWORD, NICKNAME, STUDENT_ID) VALUES (?, ?, ?, ?)",
                new Object[] {REPORTER_ID, "pw1234", "테스트신고자", REPORTER_STUDENT_ID});
        execute(
                "INSERT INTO REPORT (REPORT_ID, REPORT_TYPE, REASON, TARGET_ID, REPORTER_ID) "
                        + "VALUES (?, 'NO_SHOW', ?, ?, ?)",
                new Object[] {REPORT_ID, "테스트 신고", USER_ID, REPORTER_ID});
    }

    static void tearDown() {
        String members = "(?, ?, ?, ?)";
        Object[] ids = new Object[] {USER_ID, ADMIN_ID, REPORTER_ID, NEW_USER_ID};
        execute("DELETE FROM SANCTION WHERE TARGET_ID IN " + members, ids);
        execute(
                "DELETE FROM REPORT WHERE TARGET_ID IN " + members + " OR REPORT_ID = ?",
                new Object[] {USER_ID, ADMIN_ID, REPORTER_ID, NEW_USER_ID, REPORT_ID});
        execute("DELETE FROM MEMBER WHERE USER_ID IN " + members, ids);
        execute("DELETE FROM STUDENT WHERE SCHOOL_ID = ?", new Object[] {SCHOOL_ID});
        execute("DELETE FROM SCHOOL WHERE SCHOOL_ID = ?", new Object[] {SCHOOL_ID});
    }

    /** 제재 테스트 사이에 상태를 처음으로 되돌린다 */
    static void resetSanctions() {
        execute("DELETE FROM SANCTION WHERE TARGET_ID = ?", new Object[] {USER_ID});
        execute(
                "UPDATE MEMBER SET MEMBER_STATUS = 'ACTIVE' WHERE USER_ID = ?",
                new Object[] {USER_ID});
    }

    private static void insertStudent(Long id, String no, String name, String status) {
        execute(
                "INSERT INTO STUDENT (STUDENT_ID, SCHOOL_ID, STUDENT_NO, STUDENT_NAME, DEPARTMENT,"
                        + " EMAIL, ENROLL_STATUS) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new Object[] {
                    id, SCHOOL_ID, no, name, "테스트학과", no.toLowerCase() + "@test900001.ac.kr", status
                });
    }

    static void execute(String sql, Object[] param) {
        JDBCUtil jdbcUtil = new JDBCUtil(sql, param);
        try {
            jdbcUtil.executeUpdate();
            jdbcUtil.commit();
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new IllegalStateException(sql, ex);
        } finally {
            jdbcUtil.close();
        }
    }
}
