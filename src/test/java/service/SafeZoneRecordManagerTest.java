package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import persistence.dao.TestFixture;
import service.dto.TradeRecordInfo;
import service.exception.NotFoundException;

@Tag("db")
class SafeZoneRecordManagerTest {

    @BeforeAll
    static void setUp() {
        TestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        TestFixture.tearDown();
    }

    @Test
    void getRecordAction() {
        TradeRecordInfo seller = new TradeRecordInfo();
        seller.setMyRole("SELLER");
        TradeRecordInfo buyer = new TradeRecordInfo();
        buyer.setMyRole("BUYER");

        assertEquals("DROP_OFF", SafeZoneManager.getInstance().getRecordAction(seller));
        assertEquals("PICK_UP", SafeZoneManager.getInstance().getRecordAction(buyer));
    }

    @Test
    void isAcceptedTrade() {
        TradeRecordInfo requested = new TradeRecordInfo();
        requested.setTradeStatus("REQUESTED");
        TradeRecordInfo accepted = new TradeRecordInfo();
        accepted.setTradeStatus("ACCEPTED");
        TradeRecordInfo done = new TradeRecordInfo();
        done.setTradeStatus("DONE");

        assertFalse(SafeZoneManager.getInstance().isAcceptedTrade(requested));
        assertTrue(SafeZoneManager.getInstance().isAcceptedTrade(accepted));
        assertFalse(SafeZoneManager.getInstance().isAcceptedTrade(done));
    }

    @Test
    void isPickedUpWithoutRecord() throws Exception {
        assertFalse(SafeZoneManager.getInstance().isPickedUp(TestFixture.TRADE_ID));
    }

    @Test
    void findMissingTrade() {
        assertThrows(
                NotFoundException.class,
                () -> SafeZoneManager.getInstance().findTradeRecordInfo(-1L, TestFixture.BUYER_ID));
    }
}
