package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.ChatMessage;
import domain.ChatRoom;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class ChatMessageDAOTest {

    private final ChatMessageDAO dao = new ChatMessageDAO();
    private final ChatRoomDAO roomDao = new ChatRoomDAO();
    private Long chatRoomId;

    @BeforeEach
    void setUp() {
        TestFixture.setUp();
        ChatRoom room = new ChatRoom();
        room.setPostId(TestFixture.POST_ID);
        room.setBuyerId(TestFixture.BUYER_ID);
        chatRoomId = roomDao.insertChatRoom(room);
    }

    @AfterAll
    static void tearDown() {
        TestFixture.tearDown();
    }

    private ChatMessage textMessage(String senderId, String content) {
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setSenderId(senderId);
        message.setMessageType("TEXT");
        message.setContent(content);
        return message;
    }

    private ChatMessage systemMessage(String senderId) {
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setSenderId(senderId);
        message.setMessageType("SYSTEM");
        message.setSystemType("SAFE_ZONE_RECORDED");
        message.setPlaceName("테스트 Safe Zone");
        return message;
    }

    @Test
    void insertTextMessage() {
        Long messageId = dao.insertMessage(textMessage(TestFixture.BUYER_ID, "안녕하세요"));

        assertNotNull(messageId);
        ChatMessage saved = dao.getMessage(messageId);
        assertEquals("TEXT", saved.getMessageType());
        assertEquals("안녕하세요", saved.getContent());
        assertFalse(saved.isRead());
        assertNotNull(saved.getSentAt());
    }

    @Test
    void insertMessageUpdatesLastMessageAt() {
        dao.insertMessage(textMessage(TestFixture.BUYER_ID, "안녕하세요"));

        assertNotNull(roomDao.getChatRoom(chatRoomId).getLastMessageAt());
    }

    @Test
    void insertImageMessage() {
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setSenderId(TestFixture.SELLER_ID);
        message.setMessageType("IMAGE");
        message.setImageUrl("/upload/chat/test.jpg");

        Long messageId = dao.insertMessage(message);

        assertEquals("/upload/chat/test.jpg", dao.getMessage(messageId).getImageUrl());
    }

    @Test
    void insertSystemMessage() {
        Long messageId = dao.insertMessage(systemMessage(null));

        ChatMessage saved = dao.getMessage(messageId);
        assertNull(saved.getSenderId());
        assertEquals("SAFE_ZONE_RECORDED", saved.getSystemType());
        assertEquals("테스트 Safe Zone", saved.getPlaceName());
    }

    @Test
    void insertSystemMessageWithSenderFails() {
        assertNull(dao.insertMessage(systemMessage(TestFixture.SELLER_ID)));
        assertTrue(dao.getMessageList(chatRoomId).isEmpty());
    }

    @Test
    void getMessageListInOrder() {
        dao.insertMessage(textMessage(TestFixture.BUYER_ID, "첫 번째"));
        dao.insertMessage(textMessage(TestFixture.SELLER_ID, "두 번째"));
        dao.insertMessage(systemMessage(null));

        List<ChatMessage> list = dao.getMessageList(chatRoomId);

        assertEquals(3, list.size());
        assertEquals("첫 번째", list.get(0).getContent());
        assertEquals("SYSTEM", list.get(2).getMessageType());
    }

    @Test
    void getMessageListAfter() {
        Long firstId = dao.insertMessage(textMessage(TestFixture.BUYER_ID, "첫 번째"));
        dao.insertMessage(textMessage(TestFixture.SELLER_ID, "두 번째"));

        List<ChatMessage> list = dao.getMessageListAfter(chatRoomId, firstId);

        assertEquals(1, list.size());
        assertEquals("두 번째", list.get(0).getContent());
    }

    @Test
    void unreadCountAndUpdateRead() {
        dao.insertMessage(textMessage(TestFixture.SELLER_ID, "판매자 메시지 1"));
        dao.insertMessage(textMessage(TestFixture.SELLER_ID, "판매자 메시지 2"));
        dao.insertMessage(textMessage(TestFixture.BUYER_ID, "구매자 메시지"));
        dao.insertMessage(systemMessage(null));

        assertEquals(3, dao.getUnreadCount(chatRoomId, TestFixture.BUYER_ID));
        assertEquals(3, dao.updateRead(chatRoomId, TestFixture.BUYER_ID));
        assertEquals(0, dao.getUnreadCount(chatRoomId, TestFixture.BUYER_ID));
        assertEquals(1, dao.getUnreadCount(chatRoomId, TestFixture.SELLER_ID));
    }
}
