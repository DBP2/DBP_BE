package persistence.dao;

import domain.SafeZone;
import domain.SafeZoneRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;

public class SafeZoneRecordDAO {

    private JDBCUtil jdbcUtil = null;

    public SafeZoneRecordDAO() {
        jdbcUtil = new JDBCUtil();
    }

    private static String query =
            "SELECT SAFE_ZONE_RECORD.RECORD_ID, "
                    + "SAFE_ZONE_RECORD.TRADE_ID, "
                    + "SAFE_ZONE_RECORD.RECORDER_ID, "
                    + "SAFE_ZONE_RECORD.SAFE_ZONE_ID, "
                    + "SAFE_ZONE_RECORD.RECORD_ACTION, "
                    + "SAFE_ZONE_RECORD.RECORDED_AT, "
                    + "SAFE_ZONE.ZONE_NAME, "
                    + "SAFE_ZONE.LOCATION_DETAIL, "
                    + "SAFE_ZONE.OPEN_TIME, "
                    + "SAFE_ZONE.CLOSE_TIME "
                    + "FROM SAFE_ZONE_RECORD, SAFE_ZONE "
                    + "WHERE SAFE_ZONE_RECORD.SAFE_ZONE_ID = SAFE_ZONE.SAFE_ZONE_ID ";

    public Long insertRecord(SafeZoneRecord record) {
        Long recordId = null;
        String seqQuery = "SELECT SEQ_SAFE_ZONE_RECORD.NEXTVAL AS RECORD_ID FROM DUAL ";
        String insertQuery =
                "INSERT INTO SAFE_ZONE_RECORD "
                        + "(RECORD_ID, TRADE_ID, RECORDER_ID, SAFE_ZONE_ID, RECORD_ACTION) "
                        + "VALUES (?, ?, ?, ?, ?) ";
        String photoQuery =
                "INSERT INTO SAFE_ZONE_RECORD_PHOTO (PHOTO_ID, RECORD_ID, IMAGE_URL, PHOTO_ORDER) "
                        + "VALUES (SEQ_SAFE_ZONE_RECORD_PHOTO.NEXTVAL, ?, ?, ?) ";

        try {
            jdbcUtil.setSqlAndParameters(seqQuery, null);
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                recordId = rs.getLong("RECORD_ID");
            }

            Object[] param =
                    new Object[] {
                        recordId,
                        record.getTradeId(),
                        record.getRecorderId(),
                        record.getSafeZoneId(),
                        record.getRecordAction()
                    };
            jdbcUtil.setSqlAndParameters(insertQuery, param);
            jdbcUtil.executeUpdate();

            List<String> photoUrls = record.getPhotoUrls();
            for (int i = 0; i < photoUrls.size(); i++) {
                Object[] photoParam = new Object[] {recordId, photoUrls.get(i), i + 1};
                jdbcUtil.setSqlAndParameters(photoQuery, photoParam);
                jdbcUtil.executeUpdate();
            }

            jdbcUtil.commit();
            record.setRecordId(recordId);
            return recordId;
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            if (ex.getErrorCode() == 1) {
                System.out.println("이미 같은 거래에 같은 행동의 기록이 존재합니다.");
            } else {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    public SafeZoneRecord getRecord(Long recordId) {
        String searchQuery = query + "AND SAFE_ZONE_RECORD.RECORD_ID = ? ";
        return getRecordBy(searchQuery, new Object[] {recordId});
    }

    public SafeZoneRecord getRecordByTradeAndRecorder(Long tradeId, String recorderId) {
        String searchQuery =
                query
                        + "AND SAFE_ZONE_RECORD.TRADE_ID = ? "
                        + "AND SAFE_ZONE_RECORD.RECORDER_ID = ? ";
        return getRecordBy(searchQuery, new Object[] {tradeId, recorderId});
    }

    public SafeZoneRecord getRecordByTradeAndAction(Long tradeId, String recordAction) {
        String searchQuery =
                query
                        + "AND SAFE_ZONE_RECORD.TRADE_ID = ? "
                        + "AND SAFE_ZONE_RECORD.RECORD_ACTION = ? ";
        return getRecordBy(searchQuery, new Object[] {tradeId, recordAction});
    }

    public List<SafeZoneRecord> getRecordListByTrade(Long tradeId) {
        String searchQuery =
                query
                        + "AND SAFE_ZONE_RECORD.TRADE_ID = ? "
                        + "ORDER BY SAFE_ZONE_RECORD.RECORDED_AT ASC, "
                        + "SAFE_ZONE_RECORD.RECORD_ID ASC ";
        Object[] param = new Object[] {tradeId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            List<SafeZoneRecord> list = new ArrayList<SafeZoneRecord>();
            while (rs.next()) {
                list.add(toRecord(rs));
            }
            for (SafeZoneRecord record : list) {
                record.setPhotoUrls(getPhotoUrls(record.getRecordId()));
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private SafeZoneRecord getRecordBy(String searchQuery, Object[] param) {
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            SafeZoneRecord record = null;
            if (rs.next()) {
                record = toRecord(rs);
            }
            if (record != null) {
                record.setPhotoUrls(getPhotoUrls(record.getRecordId()));
            }
            return record;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private List<String> getPhotoUrls(Long recordId) throws Exception {
        String photoQuery =
                "SELECT IMAGE_URL FROM SAFE_ZONE_RECORD_PHOTO "
                        + "WHERE RECORD_ID = ? ORDER BY PHOTO_ORDER ASC ";
        Object[] param = new Object[] {recordId};
        jdbcUtil.setSqlAndParameters(photoQuery, param);

        ResultSet rs = jdbcUtil.executeQuery();
        List<String> photoUrls = new ArrayList<String>();
        while (rs.next()) {
            photoUrls.add(rs.getString("IMAGE_URL"));
        }
        return photoUrls;
    }

    private SafeZoneRecord toRecord(ResultSet rs) throws Exception {
        SafeZoneRecord record = new SafeZoneRecord();
        record.setRecordId(rs.getLong("RECORD_ID"));
        record.setTradeId(rs.getLong("TRADE_ID"));
        record.setRecorderId(rs.getString("RECORDER_ID"));
        record.setSafeZoneId(rs.getLong("SAFE_ZONE_ID"));
        record.setRecordAction(rs.getString("RECORD_ACTION"));
        Timestamp recordedAt = rs.getTimestamp("RECORDED_AT");
        if (recordedAt != null) {
            record.setRecordedAt(recordedAt.toLocalDateTime());
        }

        SafeZone zone = new SafeZone();
        zone.setSafeZoneId(rs.getLong("SAFE_ZONE_ID"));
        zone.setZoneName(rs.getString("ZONE_NAME"));
        zone.setLocationDetail(rs.getString("LOCATION_DETAIL"));
        zone.setOpenTime(rs.getString("OPEN_TIME"));
        zone.setCloseTime(rs.getString("CLOSE_TIME"));
        record.setSafeZone(zone);
        return record;
    }
}
