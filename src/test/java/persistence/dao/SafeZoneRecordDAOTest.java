package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import domain.ChatMessage;
import domain.ChatRoom;
import domain.SafeZoneRecord;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class SafeZoneRecordDAOTest {

    private final SafeZoneRecordDAO dao = new SafeZoneRecordDAO();

    @BeforeAll
    static void setUp() {
        TestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        TestFixture.tearDown();
    }

    @BeforeEach
    void resetFixture() {
        TestFixture.setUp();
    }

    private SafeZoneRecord newRecord(String recorderId, String action, List<String> photoUrls) {
        SafeZoneRecord record = new SafeZoneRecord();
        record.setTradeId(TestFixture.TRADE_ID);
        record.setRecorderId(recorderId);
        record.setSafeZoneId(TestFixture.SAFE_ZONE_ID);
        record.setRecordAction(action);
        record.setPhotoUrls(photoUrls);
        return record;
    }

    @Test
    void insertRecordWithPhotos() {
        SafeZoneRecord record =
                newRecord(
                        TestFixture.SELLER_ID,
                        "DROP_OFF",
                        List.of("/upload/test/drop-1.jpg", "/upload/test/drop-2.jpg"));

        Long recordId = dao.insertRecord(record);

        assertNotNull(recordId);
        SafeZoneRecord saved = dao.getRecord(recordId);
        assertEquals("DROP_OFF", saved.getRecordAction());
        assertEquals("테스트 Safe Zone", saved.getSafeZone().getZoneName());
        assertEquals(
                List.of("/upload/test/drop-1.jpg", "/upload/test/drop-2.jpg"),
                saved.getPhotoUrls());
        assertNotNull(saved.getRecordedAt());
    }

    private ChatMessage recordMessage(Long chatRoomId) {
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setMessageType("SYSTEM");
        message.setSystemType("SAFE_ZONE_RECORDED");
        message.setPlaceName("테스트 Safe Zone");
        return message;
    }

    @Test
    void insertRecordWithMessage() {
        ChatRoom room = new ChatRoom();
        room.setPostId(TestFixture.POST_ID);
        room.setBuyerId(TestFixture.BUYER_ID);
        Long chatRoomId = new ChatRoomDAO().insertChatRoom(room);

        Long recordId =
                dao.insertRecordWithMessage(
                        newRecord(TestFixture.SELLER_ID, "DROP_OFF", List.of("/a.jpg")),
                        recordMessage(chatRoomId));

        assertNotNull(recordId);
        List<ChatMessage> messages = new ChatMessageDAO().getMessageList(chatRoomId);
        assertEquals(1, messages.size());
        assertEquals("SAFE_ZONE_RECORDED", messages.get(0).getSystemType());
    }

    @Test
    void insertRecordWithMessageRollback() {
        Long recordId =
                dao.insertRecordWithMessage(
                        newRecord(TestFixture.SELLER_ID, "DROP_OFF", List.of("/a.jpg")),
                        recordMessage(-1L));

        assertNull(recordId);
        assertNull(dao.getRecordByTradeAndRecorder(TestFixture.TRADE_ID, TestFixture.SELLER_ID));
    }

    @Test
    void insertDuplicateRecordFails() {
        dao.insertRecord(newRecord(TestFixture.SELLER_ID, "DROP_OFF", List.of("/a.jpg")));

        Long duplicateId =
                dao.insertRecord(newRecord(TestFixture.SELLER_ID, "DROP_OFF", List.of("/b.jpg")));

        assertNull(duplicateId);
        assertEquals(1, dao.getRecordListByTrade(TestFixture.TRADE_ID).size());
    }

    @Test
    void insertRecordRollsBackWhenPhotoFails() {
        Long recordId =
                dao.insertRecord(
                        newRecord(
                                TestFixture.SELLER_ID,
                                "DROP_OFF",
                                List.of("/1.jpg", "/2.jpg", "/3.jpg", "/4.jpg")));

        assertNull(recordId);
        assertNull(dao.getRecordByTradeAndAction(TestFixture.TRADE_ID, "DROP_OFF"));
    }

    @Test
    void getRecordByTradeAndRecorder() {
        dao.insertRecord(newRecord(TestFixture.SELLER_ID, "DROP_OFF", List.of("/drop.jpg")));
        dao.insertRecord(newRecord(TestFixture.BUYER_ID, "PICK_UP", List.of("/pick.jpg")));

        SafeZoneRecord mine =
                dao.getRecordByTradeAndRecorder(TestFixture.TRADE_ID, TestFixture.BUYER_ID);

        assertNotNull(mine);
        assertEquals("PICK_UP", mine.getRecordAction());
        assertEquals(List.of("/pick.jpg"), mine.getPhotoUrls());
        assertNull(dao.getRecordByTradeAndRecorder(TestFixture.TRADE_ID, "nobody"));
    }

    @Test
    void getRecordByTradeAndAction() {
        dao.insertRecord(newRecord(TestFixture.SELLER_ID, "DROP_OFF", List.of("/drop.jpg")));

        SafeZoneRecord dropOff = dao.getRecordByTradeAndAction(TestFixture.TRADE_ID, "DROP_OFF");

        assertNotNull(dropOff);
        assertEquals(TestFixture.SAFE_ZONE_ID, dropOff.getSafeZoneId());
        assertNull(dao.getRecordByTradeAndAction(TestFixture.TRADE_ID, "PICK_UP"));
    }

    @Test
    void getRecordListByTrade() {
        dao.insertRecord(newRecord(TestFixture.SELLER_ID, "DROP_OFF", List.of("/drop.jpg")));
        dao.insertRecord(newRecord(TestFixture.BUYER_ID, "PICK_UP", List.of("/pick.jpg")));

        List<SafeZoneRecord> list = dao.getRecordListByTrade(TestFixture.TRADE_ID);

        assertEquals(2, list.size());
        assertEquals("DROP_OFF", list.get(0).getRecordAction());
    }
}
