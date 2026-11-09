package persistence.dao;

import persistence.util.JDBCUtil;

class TestFixture {

    static final Long SAFE_ZONE_ID = 900001L;
    static final Long OTHER_SAFE_ZONE_ID = 900002L;
    static final Long POST_ID = 900001L;
    static final Long TRADE_ID = 900001L;
    static final String SELLER_ID = "test_seller";
    static final String BUYER_ID = "test_buyer";

    static void setUp() {
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
        execute("INSERT INTO MEMBER (USER_ID) VALUES (?)", new Object[] {SELLER_ID});
        execute("INSERT INTO MEMBER (USER_ID) VALUES (?)", new Object[] {BUYER_ID});
        execute("INSERT INTO POST (POST_ID) VALUES (?)", new Object[] {POST_ID});
        execute("INSERT INTO TRADE (TRADE_ID) VALUES (?)", new Object[] {TRADE_ID});
    }

    static void tearDown() {
        execute(
                "DELETE FROM SAFE_ZONE_RECORD_PHOTO WHERE RECORD_ID IN "
                        + "(SELECT RECORD_ID FROM SAFE_ZONE_RECORD WHERE TRADE_ID = ?)",
                new Object[] {TRADE_ID});
        execute("DELETE FROM SAFE_ZONE_RECORD WHERE TRADE_ID = ?", new Object[] {TRADE_ID});
        execute("DELETE FROM POST_SAFE_ZONE WHERE POST_ID = ?", new Object[] {POST_ID});
        execute("DELETE FROM TRADE WHERE TRADE_ID = ?", new Object[] {TRADE_ID});
        execute("DELETE FROM POST WHERE POST_ID = ?", new Object[] {POST_ID});
        execute("DELETE FROM MEMBER WHERE USER_ID IN (?, ?)", new Object[] {SELLER_ID, BUYER_ID});
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
