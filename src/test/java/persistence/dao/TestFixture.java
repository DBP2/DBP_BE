package persistence.dao;

import persistence.util.JDBCUtil;

public class TestFixture {

    public static final Long SAFE_ZONE_ID = 900001L;
    public static final Long OTHER_SAFE_ZONE_ID = 900002L;
    public static final Long POST_ID = 900001L;
    public static final Long TRADE_ID = 900001L;
    public static final String SELLER_ID = "test_seller";
    public static final String BUYER_ID = "test_buyer";
    public static final Long SCHOOL_ID = 99001L;
    public static final Long SELLER_STUDENT_ID = 900001L;
    public static final Long BUYER_STUDENT_ID = 900002L;

    public static void setUp() {
        tearDown();
        execute(
                "INSERT INTO SAFE_ZONE (SAFE_ZONE_ID, ZONE_NAME, LOCATION_DETAIL, OPEN_TIME, "
                        + "CLOSE_TIME, QR_TOKEN, MANUAL_CODE) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new Object[] {
                    SAFE_ZONE_ID,
                    "테스트 Safe Zone",
                    "테스트관 1층",
                    "09:00",
                    "18:00",
                    "TEST-QR-900001",
                    "900001"
                });
        execute(
                "INSERT INTO SAFE_ZONE (SAFE_ZONE_ID, ZONE_NAME, LOCATION_DETAIL, OPEN_TIME, "
                        + "CLOSE_TIME, QR_TOKEN, MANUAL_CODE) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new Object[] {
                    OTHER_SAFE_ZONE_ID,
                    "테스트 Safe Zone 2",
                    "테스트관 2층",
                    null,
                    null,
                    "TEST-QR-900002",
                    "900002"
                });
        execute(
                "INSERT INTO SCHOOL (SCHOOL_ID, SCHOOL_NAME, EMAIL_DOMAIN) VALUES (?, ?, ?)",
                new Object[] {SCHOOL_ID, "테스트대학교", "test.campusmarket.ac.kr"});
        execute(
                "INSERT INTO STUDENT (STUDENT_ID, SCHOOL_ID, STUDENT_NO, STUDENT_NAME, "
                        + "DEPARTMENT, EMAIL) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] {
                    SELLER_STUDENT_ID,
                    SCHOOL_ID,
                    "T900001",
                    "테스트판매자",
                    "테스트학과",
                    "seller@test.campusmarket.ac.kr"
                });
        execute(
                "INSERT INTO STUDENT (STUDENT_ID, SCHOOL_ID, STUDENT_NO, STUDENT_NAME, "
                        + "DEPARTMENT, EMAIL) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] {
                    BUYER_STUDENT_ID,
                    SCHOOL_ID,
                    "T900002",
                    "테스트구매자",
                    "테스트학과",
                    "buyer@test.campusmarket.ac.kr"
                });
        execute(
                "INSERT INTO MEMBER (USER_ID, PASSWORD, NICKNAME, STUDENT_ID) VALUES (?, ?, ?, ?)",
                new Object[] {SELLER_ID, "test1234", "테스트판매자", SELLER_STUDENT_ID});
        execute(
                "INSERT INTO MEMBER (USER_ID, PASSWORD, NICKNAME, STUDENT_ID) VALUES (?, ?, ?, ?)",
                new Object[] {BUYER_ID, "test1234", "테스트구매자", BUYER_STUDENT_ID});
        execute("INSERT INTO POST (POST_ID) VALUES (?)", new Object[] {POST_ID});
        execute("INSERT INTO TRADE (TRADE_ID) VALUES (?)", new Object[] {TRADE_ID});
    }

    public static void tearDown() {
        execute(
                "DELETE FROM SAFE_ZONE_RECORD_PHOTO WHERE RECORD_ID IN "
                        + "(SELECT RECORD_ID FROM SAFE_ZONE_RECORD WHERE TRADE_ID = ?)",
                new Object[] {TRADE_ID});
        execute("DELETE FROM SAFE_ZONE_RECORD WHERE TRADE_ID = ?", new Object[] {TRADE_ID});
        execute(
                "DELETE FROM CHAT_MESSAGE WHERE CHAT_ROOM_ID IN "
                        + "(SELECT CHAT_ROOM_ID FROM CHAT_ROOM WHERE POST_ID = ?)",
                new Object[] {POST_ID});
        execute("DELETE FROM CHAT_ROOM WHERE POST_ID = ?", new Object[] {POST_ID});
        execute("DELETE FROM POST_SAFE_ZONE WHERE POST_ID = ?", new Object[] {POST_ID});
        execute("DELETE FROM TRADE WHERE TRADE_ID = ?", new Object[] {TRADE_ID});
        execute("DELETE FROM POST WHERE POST_ID = ?", new Object[] {POST_ID});
        execute("DELETE FROM MEMBER WHERE USER_ID IN (?, ?)", new Object[] {SELLER_ID, BUYER_ID});
        execute(
                "DELETE FROM STUDENT WHERE STUDENT_ID IN (?, ?)",
                new Object[] {SELLER_STUDENT_ID, BUYER_STUDENT_ID});
        execute("DELETE FROM SCHOOL WHERE SCHOOL_ID = ?", new Object[] {SCHOOL_ID});
        execute(
                "DELETE FROM SAFE_ZONE WHERE SAFE_ZONE_ID IN (?, ?)",
                new Object[] {SAFE_ZONE_ID, OTHER_SAFE_ZONE_ID});
    }

    private static void execute(String sql, Object[] param) {
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
