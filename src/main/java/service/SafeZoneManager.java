package service;

import domain.SafeZone;
import domain.SafeZoneRecord;
import java.sql.SQLException;
import java.util.List;
import persistence.dao.SafeZoneDAO;
import persistence.dao.SafeZoneRecordDAO;
import service.dto.TradeRecordInfo;
import service.exception.BadRequestException;
import service.exception.ConflictException;
import service.exception.ForbiddenException;
import service.exception.NotFoundException;

public class SafeZoneManager {
    public static final int MAX_PHOTO_COUNT = 3;

    private static SafeZoneManager safeZoneMan = new SafeZoneManager();
    private SafeZoneDAO safeZoneDAO;
    private SafeZoneRecordDAO recordDAO;

    private SafeZoneManager() {
        try {
            safeZoneDAO = new SafeZoneDAO();
            recordDAO = new SafeZoneRecordDAO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SafeZoneManager getInstance() {
        return safeZoneMan;
    }

    public List<SafeZone> findSafeZoneList() throws SQLException {
        return safeZoneDAO.getSafeZoneList();
    }

    public TradeRecordInfo findTradeRecordInfo(Long tradeId, String userId)
            throws SQLException, NotFoundException, ForbiddenException {
        TradeRecordInfo info = recordDAO.getTradeRecordInfo(tradeId, userId);
        if (info == null) {
            throw new NotFoundException("거래를 찾을 수 없어요");
        }
        if (info.getMyRole() == null) {
            throw new ForbiddenException("참여하지 않은 거래예요");
        }
        return info;
    }

    public SafeZone verifySafeZone(Long tradeId, String userId, String type, String value)
            throws SQLException, NotFoundException, ForbiddenException, BadRequestException {
        TradeRecordInfo info = findTradeRecordInfo(tradeId, userId);

        SafeZone safeZone = null;
        if ("QR".equals(type)) {
            safeZone = safeZoneDAO.getSafeZoneByQrToken(value);
        } else if ("CODE".equals(type)) {
            safeZone = safeZoneDAO.getSafeZoneByManualCode(value);
        } else {
            throw new BadRequestException("확인 방법이 올바르지 않아요");
        }
        if (safeZone == null) {
            throw new BadRequestException("코드가 맞지 않아요");
        }

        checkSafeZone(info, safeZone);
        return safeZone;
    }

    public SafeZoneRecord findMyRecord(Long tradeId, String userId)
            throws SQLException, NotFoundException, ForbiddenException {
        findTradeRecordInfo(tradeId, userId);

        return recordDAO.getRecordByTradeAndRecorder(tradeId, userId);
    }

    public SafeZoneRecord createRecord(SafeZoneRecord record)
            throws SQLException,
                    NotFoundException,
                    ForbiddenException,
                    BadRequestException,
                    ConflictException {
        TradeRecordInfo info = findTradeRecordInfo(record.getTradeId(), record.getRecorderId());

        if (getRecordAction(info).equals(record.getRecordAction()) == false) {
            throw new BadRequestException("할 수 없는 기록이에요");
        }
        if (record.getPhotoUrls() == null
                || record.getPhotoUrls().isEmpty()
                || record.getPhotoUrls().size() > MAX_PHOTO_COUNT) {
            throw new BadRequestException("사진은 1~3장 올려 주세요");
        }

        SafeZone safeZone = safeZoneDAO.getSafeZone(record.getSafeZoneId());
        if (safeZone == null) {
            throw new BadRequestException("장소 정보가 올바르지 않아요");
        }
        checkSafeZone(info, safeZone);

        if (recordDAO.getRecordByTradeAndRecorder(record.getTradeId(), record.getRecorderId())
                != null) {
            throw new ConflictException("이미 기록을 저장했어요");
        }

        Long recordId = recordDAO.insertRecord(record);
        if (recordId == null) {
            throw new SQLException("기록을 저장하지 못했어요");
        }

        ChatManager.getInstance()
                .addSystemMessage(
                        record.getTradeId(), "SAFE_ZONE_RECORDED", safeZone.getZoneName());
        return recordDAO.getRecord(recordId);
    }

    public String getRecordAction(TradeRecordInfo info) {
        if ("SELLER".equals(info.getMyRole())) {
            return "DROP_OFF";
        }
        return "PICK_UP";
    }

    private void checkSafeZone(TradeRecordInfo info, SafeZone safeZone) throws BadRequestException {
        if ("DROP_OFF".equals(getRecordAction(info))) {
            if (safeZoneDAO.isPostSafeZone(info.getPostId(), safeZone.getSafeZoneId()) == false) {
                throw new BadRequestException("판매자가 고른 Safe Zone이 아니에요");
            }
            return;
        }

        SafeZoneRecord dropOff = recordDAO.getRecordByTradeAndAction(info.getTradeId(), "DROP_OFF");
        if (dropOff == null) {
            throw new BadRequestException("아직 물건이 맡겨지지 않았어요");
        }
        if (dropOff.getSafeZoneId().equals(safeZone.getSafeZoneId()) == false) {
            throw new BadRequestException("물건을 맡긴 Safe Zone이 아니에요");
        }
    }
}
