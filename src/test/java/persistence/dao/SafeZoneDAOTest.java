package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.SafeZone;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class SafeZoneDAOTest {

    private final SafeZoneDAO dao = new SafeZoneDAO();

    @BeforeAll
    static void setUp() {
        TestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        TestFixture.tearDown();
    }

    @AfterEach
    void clearPostSafeZones() {
        dao.deletePostSafeZones(TestFixture.POST_ID);
    }

    @Test
    void getSafeZoneList() {
        List<SafeZone> list = dao.getSafeZoneList();

        assertNotNull(list);
        assertTrue(
                list.stream()
                        .anyMatch(zone -> zone.getSafeZoneId().equals(TestFixture.SAFE_ZONE_ID)));
    }

    @Test
    void getSafeZone() {
        SafeZone zone = dao.getSafeZone(TestFixture.SAFE_ZONE_ID);

        assertNotNull(zone);
        assertEquals("테스트 Safe Zone", zone.getZoneName());
        assertEquals("09:00", zone.getOpenTime());
    }

    @Test
    void getSafeZoneByQrToken() {
        SafeZone zone = dao.getSafeZoneByQrToken("TEST-QR-900001");

        assertNotNull(zone);
        assertEquals(TestFixture.SAFE_ZONE_ID, zone.getSafeZoneId());
        assertNull(dao.getSafeZoneByQrToken("WRONG-QR"));
    }

    @Test
    void getSafeZoneByManualCode() {
        SafeZone zone = dao.getSafeZoneByManualCode("900002");

        assertNotNull(zone);
        assertEquals(TestFixture.OTHER_SAFE_ZONE_ID, zone.getSafeZoneId());
        assertNull(zone.getOpenTime());
        assertNull(dao.getSafeZoneByManualCode("000000"));
    }

    @Test
    void insertAndGetPostSafeZones() {
        int result =
                dao.insertPostSafeZones(
                        TestFixture.POST_ID,
                        List.of(TestFixture.SAFE_ZONE_ID, TestFixture.OTHER_SAFE_ZONE_ID));

        assertEquals(2, result);
        assertEquals(2, dao.getSafeZoneListByPost(TestFixture.POST_ID).size());
        assertTrue(dao.isPostSafeZone(TestFixture.POST_ID, TestFixture.SAFE_ZONE_ID));
    }

    @Test
    void isPostSafeZoneFalse() {
        dao.insertPostSafeZones(TestFixture.POST_ID, List.of(TestFixture.SAFE_ZONE_ID));

        assertFalse(dao.isPostSafeZone(TestFixture.POST_ID, TestFixture.OTHER_SAFE_ZONE_ID));
    }

    @Test
    void deletePostSafeZones() {
        dao.insertPostSafeZones(TestFixture.POST_ID, List.of(TestFixture.SAFE_ZONE_ID));

        assertEquals(1, dao.deletePostSafeZones(TestFixture.POST_ID));
        assertTrue(dao.getSafeZoneListByPost(TestFixture.POST_ID).isEmpty());
    }
}
