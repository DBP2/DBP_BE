package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SafeZoneTest {

    @Test
    void operatingHours() {
        SafeZone safeZone = new SafeZone();
        safeZone.setOpenTime("08:00");
        safeZone.setCloseTime("21:00");

        assertEquals("08:00–21:00", safeZone.getOperatingHours());
    }

    @Test
    void operatingHoursAllDay() {
        assertEquals("24시간", new SafeZone().getOperatingHours());
    }
}
