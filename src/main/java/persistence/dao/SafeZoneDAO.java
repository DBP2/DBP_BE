package persistence.dao;

import domain.SafeZone;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;

public class SafeZoneDAO {

    private JDBCUtil jdbcUtil = null;

    public SafeZoneDAO() {
        jdbcUtil = new JDBCUtil();
    }

    private static String query =
            "SELECT SAFE_ZONE.SAFE_ZONE_ID, "
                    + "SAFE_ZONE.ZONE_NAME, "
                    + "SAFE_ZONE.LOCATION_DETAIL, "
                    + "SAFE_ZONE.OPEN_TIME, "
                    + "SAFE_ZONE.CLOSE_TIME, "
                    + "SAFE_ZONE.QR_TOKEN, "
                    + "SAFE_ZONE.MANUAL_CODE ";

    public List<SafeZone> getSafeZoneList() {
        String allQuery = query + "FROM SAFE_ZONE ORDER BY SAFE_ZONE_ID ASC ";
        jdbcUtil.setSqlAndParameters(allQuery, null);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            List<SafeZone> list = new ArrayList<SafeZone>();
            while (rs.next()) {
                list.add(toSafeZone(rs));
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    public SafeZone getSafeZone(Long safeZoneId) {
        String searchQuery = query + "FROM SAFE_ZONE WHERE SAFE_ZONE_ID = ? ";
        return getSafeZoneBy(searchQuery, safeZoneId);
    }

    public SafeZone getSafeZoneByQrToken(String qrToken) {
        String searchQuery = query + "FROM SAFE_ZONE WHERE QR_TOKEN = ? ";
        return getSafeZoneBy(searchQuery, qrToken);
    }

    public SafeZone getSafeZoneByManualCode(String manualCode) {
        String searchQuery = query + "FROM SAFE_ZONE WHERE MANUAL_CODE = ? ";
        return getSafeZoneBy(searchQuery, manualCode);
    }

    public List<SafeZone> getSafeZoneListByPost(Long postId) {
        String searchQuery =
                query
                        + "FROM SAFE_ZONE, POST_SAFE_ZONE "
                        + "WHERE POST_SAFE_ZONE.POST_ID = ? AND "
                        + "POST_SAFE_ZONE.SAFE_ZONE_ID = SAFE_ZONE.SAFE_ZONE_ID "
                        + "ORDER BY SAFE_ZONE.SAFE_ZONE_ID ASC ";
        Object[] param = new Object[] {postId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            List<SafeZone> list = new ArrayList<SafeZone>();
            while (rs.next()) {
                list.add(toSafeZone(rs));
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    public boolean isPostSafeZone(Long postId, Long safeZoneId) {
        String searchQuery =
                "SELECT COUNT(*) AS CNT FROM POST_SAFE_ZONE "
                        + "WHERE POST_ID = ? AND SAFE_ZONE_ID = ? ";
        Object[] param = new Object[] {postId, safeZoneId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

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

    public int insertPostSafeZones(Long postId, List<Long> safeZoneIds) {
        int result = 0;
        String insertQuery = "INSERT INTO POST_SAFE_ZONE (POST_ID, SAFE_ZONE_ID) VALUES (?, ?) ";

        try {
            for (Long safeZoneId : safeZoneIds) {
                Object[] param = new Object[] {postId, safeZoneId};
                jdbcUtil.setSqlAndParameters(insertQuery, param);
                result += jdbcUtil.executeUpdate();
            }
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

    public int deletePostSafeZones(Long postId) {
        String deleteQuery = "DELETE FROM POST_SAFE_ZONE WHERE POST_ID = ? ";
        Object[] param = new Object[] {postId};
        jdbcUtil.setSqlAndParameters(deleteQuery, param);

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

    private SafeZone getSafeZoneBy(String searchQuery, Object value) {
        Object[] param = new Object[] {value};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            SafeZone zone = null;
            if (rs.next()) {
                zone = toSafeZone(rs);
            }
            return zone;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private SafeZone toSafeZone(ResultSet rs) throws Exception {
        SafeZone zone = new SafeZone();
        zone.setSafeZoneId(rs.getLong("SAFE_ZONE_ID"));
        zone.setZoneName(rs.getString("ZONE_NAME"));
        zone.setLocationDetail(rs.getString("LOCATION_DETAIL"));
        zone.setOpenTime(rs.getString("OPEN_TIME"));
        zone.setCloseTime(rs.getString("CLOSE_TIME"));
        zone.setQrToken(rs.getString("QR_TOKEN"));
        zone.setManualCode(rs.getString("MANUAL_CODE"));
        return zone;
    }
}
