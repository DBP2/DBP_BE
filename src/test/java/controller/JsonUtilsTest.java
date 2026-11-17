package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonUtilsTest {

    @Test
    void formatDateTimeWithOffset() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 11, 6, 17, 21);

        assertEquals("2026-11-06T17:21:00+09:00", JsonUtils.formatDateTime(dateTime));
        assertNull(JsonUtils.formatDateTime(null));
    }

    @Test
    void writeLocalDateTimeAsIsoString() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2026, 11, 6, 17, 21);

        assertEquals(
                "{\"createdAt\":\"2026-11-06T17:21:00+09:00\"}",
                JsonUtils.toJson(Map.of("createdAt", dateTime)));
    }

    @Test
    void convertId() {
        assertEquals("12", JsonUtils.toId(12L));
        assertNull(JsonUtils.toId(null));
        assertEquals(12L, JsonUtils.parseId("12"));
        assertNull(JsonUtils.parseId("zone-2"));
    }
}
