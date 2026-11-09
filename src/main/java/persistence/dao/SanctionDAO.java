package persistence.dao;

import domain.Sanction;
import domain.User;

import persistence.util.JDBCUtil;

import service.dto.SanctionTarget;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** SANCTION (+ MEMBER 상태 변경). 제재 적용·해제는 두 테이블을 함께 바꾸므로 한 트랜잭션으로 처리 */
public class SanctionDAO {

    private JDBCUtil jdbcUtil = null;

    public SanctionDAO() {
        jdbcUtil = new JDBCUtil();
    }

    private static String query =
            "SELECT s.SANCTION_ID, s.TARGET_ID, m.NICKNAME, s.ADMIN_ID, s.REPORT_ID,"
                + " s.SANCTION_TYPE, s.REASON, s.START_AT, s.END_AT, s.IS_RELEASED, s.RELEASED_AT"
                + " FROM SANCTION s JOIN MEMBER m ON s.TARGET_ID = m.USER_ID ";

    /** 현재 유효한 제재 조건 (해제 안 됐고 기간이 남음) */
    private static String activeCond =
            "s.IS_RELEASED = 'N' AND (s.END_AT IS NULL OR s.END_AT > SYSDATE) ";

    /** 유효한 정지(기간·영구)가 남아 있지 않은 회원 조건 */
    private static String noRestriction =
            "NOT EXISTS (SELECT 1 FROM SANCTION s WHERE s.TARGET_ID = m.USER_ID "
                    + "AND s.SANCTION_TYPE IN ('SUSPENSION', 'BAN') AND "
                    + activeCond
                    + ")";

    /**
     * 제재 적용 [트랜잭션]: SANCTION INSERT + (기간 정지·영구 정지면) MEMBER_STATUS 변경. 하나라도 실패하면 모두 취소한다.
     *
     * @return 새 SANCTION_ID (실패하면 null)
     */
    public Long applySanction(Sanction s) {
        try {
            jdbcUtil.setSqlAndParameters("SELECT SEQ_SANCTION.NEXTVAL AS ID FROM DUAL", null);
            ResultSet rs = jdbcUtil.executeQuery();
            rs.next();
            Long id = rs.getLong("ID");

            String insertQuery =
                    "INSERT INTO SANCTION (SANCTION_ID, TARGET_ID, ADMIN_ID, REPORT_ID,"
                            + " SANCTION_TYPE, REASON, START_AT, END_AT) VALUES (?, ?, ?, ?, ?, ?,"
                            + " SYSDATE, ?)";
            jdbcUtil.setSqlAndParameters(
                    insertQuery,
                    new Object[] {
                        id,
                        s.getTargetId(),
                        s.getAdminId(),
                        s.getReportId(),
                        s.getSanctionType(),
                        s.getReason(),
                        toTimestamp(s.getEndAt())
                    });
            jdbcUtil.executeUpdate();

            String status = null;
            if (Sanction.SUSPENSION.equals(s.getSanctionType())) {
                status = User.SUSPENDED;
            } else if (Sanction.BAN.equals(s.getSanctionType())) {
                status = User.BANNED;
            }
            if (status != null) {
                jdbcUtil.setSqlAndParameters(
                        "UPDATE MEMBER SET MEMBER_STATUS = ? WHERE USER_ID = ?",
                        new Object[] {status, s.getTargetId()});
                jdbcUtil.executeUpdate();
            }
            jdbcUtil.commit();
            s.setSanctionId(id);
            return id;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    /**
     * 제재 해제 [트랜잭션]: SANCTION 을 해제 처리하고, 남은 유효한 정지가 없으면 MEMBER_STATUS 를 ACTIVE 로.
     *
     * @return 해제된 제재 수 (이미 해제됐으면 0)
     */
    public int release(Long sanctionId) {
        try {
            jdbcUtil.setSqlAndParameters(
                    "UPDATE SANCTION SET IS_RELEASED = 'Y', RELEASED_AT = SYSDATE "
                            + "WHERE SANCTION_ID = ? AND IS_RELEASED = 'N'",
                    new Object[] {sanctionId});
            int n = jdbcUtil.executeUpdate();
            if (n > 0) {
                jdbcUtil.setSqlAndParameters(
                        "UPDATE MEMBER m SET MEMBER_STATUS = 'ACTIVE' WHERE m.USER_ID = (SELECT"
                            + " TARGET_ID FROM SANCTION WHERE SANCTION_ID = ?) AND m.MEMBER_STATUS"
                            + " IN ('SUSPENDED', 'BANNED') AND "
                                + noRestriction,
                        new Object[] {sanctionId});
                jdbcUtil.executeUpdate();
            }
            jdbcUtil.commit();
            return n;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return 0;
    }

    /** 기간이 끝난 정지 반영: 유효한 정지가 남아 있지 않으면 SUSPENDED → ACTIVE (로그인할 때 호출) */
    public int refreshStatus(String userId) {
        jdbcUtil.setSqlAndParameters(
                "UPDATE MEMBER m SET MEMBER_STATUS = 'ACTIVE' "
                        + "WHERE m.USER_ID = ? AND m.MEMBER_STATUS = 'SUSPENDED' AND "
                        + noRestriction,
                new Object[] {userId});
        try {
            int n = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return n;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return 0;
    }

    /** 제재 1건 조회 */
    public Sanction findSanction(Long sanctionId) {
        List<Sanction> list = findBy(query + "WHERE s.SANCTION_ID = ?", new Object[] {sanctionId});
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /** 현재 유효한 제재 중 가장 무거운·최근 것 (없으면 null). 영구 정지 > 기간 정지 > 경고 */
    public Sanction findActiveByUser(String userId) {
        String sql =
                query
                        + "WHERE s.TARGET_ID = ? AND "
                        + activeCond
                        + "ORDER BY CASE s.SANCTION_TYPE WHEN 'BAN' THEN 0 WHEN 'SUSPENSION' THEN 1"
                        + " ELSE 2 END, s.START_AT DESC FETCH FIRST 1 ROWS ONLY";
        List<Sanction> list = findBy(sql, new Object[] {userId});
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /** 회원의 제재 이력 (최근 순) */
    public List<Sanction> findListByUser(String userId) {
        return findBy(
                query + "WHERE s.TARGET_ID = ? ORDER BY s.START_AT DESC, s.SANCTION_ID DESC",
                new Object[] {userId});
    }

    /**
     * 관리자 제재 대상 목록. 미처리 신고 = 아직 어떤 제재의 REPORT_ID 로도 연결되지 않은 신고.
     *
     * @param filter "pending"(미처리 신고 있음, 기본) / "sanctioned"(정지 중) / "all"
     */
    public List<SanctionTarget> findTargetList(String filter) {
        String having;
        if ("sanctioned".equals(filter)) {
            having = "HAVING m.MEMBER_STATUS IN ('SUSPENDED', 'BANNED') ";
        } else if ("all".equals(filter)) {
            having = "HAVING COUNT(r.REPORT_ID) > 0 OR m.MEMBER_STATUS IN ('SUSPENDED', 'BANNED') ";
        } else {
            having =
                    "HAVING COUNT(DISTINCT CASE WHEN sr.SANCTION_ID IS NULL THEN r.REPORT_ID END) >"
                            + " 0 ";
        }
        String sql =
                "SELECT m.USER_ID, m.NICKNAME, m.TRUST_SCORE, m.MEMBER_STATUS, COUNT(DISTINCT"
                    + " r.REPORT_ID) AS REPORT_COUNT, COUNT(DISTINCT CASE WHEN sr.SANCTION_ID IS"
                    + " NULL THEN r.REPORT_ID END) AS PENDING_COUNT FROM MEMBER m LEFT JOIN REPORT"
                    + " r ON r.TARGET_ID = m.USER_ID LEFT JOIN SANCTION sr ON sr.REPORT_ID ="
                    + " r.REPORT_ID GROUP BY m.USER_ID, m.NICKNAME, m.TRUST_SCORE, m.MEMBER_STATUS "
                        + having
                        + "ORDER BY PENDING_COUNT DESC, REPORT_COUNT DESC, m.USER_ID";
        jdbcUtil.setSqlAndParameters(sql, null);
        List<SanctionTarget> list = new ArrayList<SanctionTarget>();
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            while (rs.next()) {
                SanctionTarget t = new SanctionTarget();
                t.setUserId(rs.getString("USER_ID"));
                t.setNickname(rs.getString("NICKNAME"));
                t.setTrustScore(rs.getInt("TRUST_SCORE"));
                t.setMemberStatus(rs.getString("MEMBER_STATUS"));
                t.setReportCount(rs.getInt("REPORT_COUNT"));
                t.setPendingCount(rs.getInt("PENDING_COUNT"));
                list.add(t);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            jdbcUtil.close();
        }
        for (SanctionTarget t : list) { // 연결을 닫은 뒤 회원별 현재 제재를 채운다
            t.setActiveSanction(findActiveByUser(t.getUserId()));
        }
        return list;
    }

    private List<Sanction> findBy(String sql, Object[] param) {
        jdbcUtil.setSqlAndParameters(sql, param);
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            List<Sanction> list = new ArrayList<Sanction>();
            while (rs.next()) {
                list.add(toSanction(rs));
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private static Timestamp toTimestamp(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    private static LocalDateTime toLocalDateTime(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }

    private static Sanction toSanction(ResultSet rs) throws SQLException {
        Sanction s = new Sanction();
        s.setSanctionId(rs.getLong("SANCTION_ID"));
        s.setTargetId(rs.getString("TARGET_ID"));
        s.setTargetNickname(rs.getString("NICKNAME"));
        s.setAdminId(rs.getString("ADMIN_ID"));
        long reportId = rs.getLong("REPORT_ID");
        s.setReportId(rs.wasNull() ? null : reportId);
        s.setSanctionType(rs.getString("SANCTION_TYPE"));
        s.setReason(rs.getString("REASON"));
        s.setStartAt(toLocalDateTime(rs.getTimestamp("START_AT")));
        s.setEndAt(toLocalDateTime(rs.getTimestamp("END_AT")));
        s.setReleased("Y".equals(rs.getString("IS_RELEASED")));
        s.setReleasedAt(toLocalDateTime(rs.getTimestamp("RELEASED_AT")));
        return s;
    }
}
