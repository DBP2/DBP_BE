package controller.safezone.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import domain.SafeZone;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SafeZoneResponseTest {

    @Test
    void toMap() {
        SafeZone safeZone = new SafeZone();
        safeZone.setSafeZoneId(2L);
        safeZone.setZoneName("중앙도서관 Safe Zone");
        safeZone.setLocationDetail("중앙도서관 정문 옆 2-1 사물함");
        safeZone.setOpenTime("08:00");
        safeZone.setCloseTime("21:00");
        safeZone.setQrToken("SZ-QR-LIBRARY-02");
        safeZone.setManualCode("482713");

        Map<String, Object> map = SafeZoneResponse.toMap(safeZone);

        assertEquals("2", map.get("id"));
        assertEquals("중앙도서관 Safe Zone", map.get("name"));
        assertEquals("중앙도서관 정문 옆 2-1 사물함", map.get("location"));
        assertEquals("08:00–21:00", map.get("operatingHours"));
        assertFalse(map.containsKey("qrToken"));
        assertFalse(map.containsKey("manualCode"));
    }
}
