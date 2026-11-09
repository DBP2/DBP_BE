package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import domain.ChatRoom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class ChatRoomDAOTest {

    private final ChatRoomDAO dao = new ChatRoomDAO();

    @BeforeEach
    void setUp() {
        TestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        TestFixture.tearDown();
    }

    private ChatRoom newRoom() {
        ChatRoom room = new ChatRoom();
        room.setPostId(TestFixture.POST_ID);
        room.setBuyerId(TestFixture.BUYER_ID);
        return room;
    }

    @Test
    void insertAndGetChatRoom() {
        Long chatRoomId = dao.insertChatRoom(newRoom());

        assertNotNull(chatRoomId);
        ChatRoom room = dao.getChatRoom(chatRoomId);
        assertEquals(TestFixture.POST_ID, room.getPostId());
        assertEquals(TestFixture.BUYER_ID, room.getBuyerId());
        assertNull(room.getTradeId());
        assertNotNull(room.getCreatedAt());
    }

    @Test
    void getChatRoomByPostAndBuyer() {
        Long chatRoomId = dao.insertChatRoom(newRoom());

        ChatRoom room =
                dao.getChatRoomByPostAndBuyer(TestFixture.POST_ID, TestFixture.BUYER_ID);

        assertEquals(chatRoomId, room.getChatRoomId());
        assertNull(dao.getChatRoomByPostAndBuyer(TestFixture.POST_ID, TestFixture.SELLER_ID));
    }

    @Test
    void insertDuplicateChatRoomFails() {
        dao.insertChatRoom(newRoom());

        assertNull(dao.insertChatRoom(newRoom()));
    }

    @Test
    void updateTrade() {
        Long chatRoomId = dao.insertChatRoom(newRoom());

        assertEquals(1, dao.updateTrade(chatRoomId, TestFixture.TRADE_ID));
        assertEquals(chatRoomId, dao.getChatRoomByTrade(TestFixture.TRADE_ID).getChatRoomId());
    }

    @Test
    void updateLastMessageAt() {
        Long chatRoomId = dao.insertChatRoom(newRoom());

        assertEquals(1, dao.updateLastMessageAt(chatRoomId));
        assertNotNull(dao.getChatRoom(chatRoomId).getLastMessageAt());
    }
}
