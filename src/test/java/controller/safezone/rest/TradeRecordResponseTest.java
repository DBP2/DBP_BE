package controller.safezone.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import domain.SafeZone;
import domain.SafeZoneRecord;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import service.dto.TradeRecordInfo;

class TradeRecordResponseTest {

    private HttpServletRequest request =
            (HttpServletRequest)
                    Proxy.newProxyInstance(
                            getClass().getClassLoader(),
                            new Class<?>[] {HttpServletRequest.class},
                            (proxy, method, args) ->
                                    switch (method.getName()) {
                                        case "getScheme" -> "http";
                                        case "getServerName" -> "localhost";
                                        case "getServerPort" -> 8080;
                                        case "getContextPath" -> "/campusmarket";
                                        default -> null;
                                    });

    @Test
    void toInfoMap() {
        TradeRecordInfo info = new TradeRecordInfo();
        info.setTradeId(5L);
        info.setChatRoomId(7L);
        info.setPostTitle("데이터베이스 개론");
        info.setPrice(9000);
        info.setTradeStatus("ACCEPTED");
        info.setMyRole("SELLER");

        Map<String, Object> map = TradeRecordResponse.toInfoMap(info);

        assertEquals("5", map.get("tradeId"));
        assertEquals("7", map.get("roomId"));
        assertEquals(9000, map.get("price"));
        assertEquals("accepted", map.get("tradeStatus"));
        assertEquals("seller", map.get("myRole"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toRecordMap() {
        SafeZone safeZone = new SafeZone();
        safeZone.setSafeZoneId(1L);
        safeZone.setZoneName("숭인관 Safe Zone");
        safeZone.setQrToken("SZ-QR-SUNGIN-01");

        SafeZoneRecord record = new SafeZoneRecord();
        record.setRecordId(3L);
        record.setTradeId(5L);
        record.setSafeZone(safeZone);
        record.setRecordAction("DROP_OFF");
        record.setPhotoUrls(List.of("/uploads/safe-zone/a.jpg"));
        record.setRecordedAt(LocalDateTime.of(2026, 11, 17, 15, 0));

        Map<String, Object> map = TradeRecordResponse.toRecordMap(request, record);
        Map<String, Object> zone = (Map<String, Object>) map.get("safeZone");

        assertEquals("3", map.get("id"));
        assertEquals("DROP_OFF", map.get("action"));
        assertEquals("숭인관 Safe Zone", zone.get("name"));
        assertNull(zone.get("qrToken"));
        assertNull(map.get("lockerNumber"));
        assertEquals(
                List.of("http://localhost:8080/campusmarket/uploads/safe-zone/a.jpg"),
                map.get("photoUrls"));
        assertEquals("2026-11-17T15:00:00+09:00", map.get("recordedAt"));
    }
}
