package controller.safezone.rest;

import controller.FileUploadUtils;
import controller.JsonUtils;
import domain.SafeZone;
import domain.SafeZoneRecord;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import service.dto.TradeRecordInfo;

public class TradeRecordResponse {

    public static Map<String, Object> toVerificationMap(SafeZone safeZone) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("safeZone", SafeZoneResponse.toMap(safeZone));
        map.put("lockerNumber", null);
        map.put("verifiedAt", JsonUtils.formatDateTime(LocalDateTime.now(JsonUtils.ZONE)));
        return map;
    }

    public static Map<String, Object> toInfoMap(TradeRecordInfo info) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("tradeId", JsonUtils.toId(info.getTradeId()));
        map.put("roomId", JsonUtils.toId(info.getChatRoomId()));
        map.put("postTitle", info.getPostTitle());
        map.put("price", info.getPrice());
        map.put("tradeStatus", toLowerCase(info.getTradeStatus()));
        map.put("myRole", toLowerCase(info.getMyRole()));
        return map;
    }

    public static Map<String, Object> toRecordMap(
            HttpServletRequest request, SafeZoneRecord record) {
        List<String> photoUrls = new ArrayList<String>();
        for (String photoUrl : record.getPhotoUrls()) {
            photoUrls.add(FileUploadUtils.toUrl(request, photoUrl));
        }

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", JsonUtils.toId(record.getRecordId()));
        map.put("tradeId", JsonUtils.toId(record.getTradeId()));
        map.put("safeZone", SafeZoneResponse.toMap(record.getSafeZone()));
        map.put("lockerNumber", null);
        map.put("action", record.getRecordAction());
        map.put("photoUrls", photoUrls);
        map.put("recordedAt", JsonUtils.formatDateTime(record.getRecordedAt()));
        return map;
    }

    private static String toLowerCase(String value) {
        return value == null ? null : value.toLowerCase();
    }
}
