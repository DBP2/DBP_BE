package service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.SafeZone;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import persistence.dao.TestFixture;

@Tag("db")
class SafeZoneManagerTest {

    @BeforeAll
    static void setUp() {
        TestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        TestFixture.tearDown();
    }

    @Test
    void findSafeZoneList() throws Exception {
        List<SafeZone> list = SafeZoneManager.getInstance().findSafeZoneList();

        assertNotNull(list);
        assertTrue(
                list.stream()
                        .anyMatch(zone -> zone.getSafeZoneId().equals(TestFixture.SAFE_ZONE_ID)));
    }
}
