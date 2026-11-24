package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import persistence.dao.TestFixture;
import service.exception.BadRequestException;
import service.exception.NotFoundException;

@Tag("db")
class ChatManagerTest {

    @BeforeAll
    static void setUp() {
        TestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        TestFixture.tearDown();
    }

    @Test
    void getTrustGrade() {
        ChatManager chatMan = ChatManager.getInstance();

        assertEquals("A", chatMan.getTrustGrade(30));
        assertEquals("B", chatMan.getTrustGrade(10));
        assertEquals("C", chatMan.getTrustGrade(0));
        assertEquals("D", chatMan.getTrustGrade(-1));
    }

    @Test
    void sendBlankMessage() {
        assertThrows(
                BadRequestException.class,
                () -> ChatManager.getInstance().sendMessage(1L, TestFixture.BUYER_ID, " "));
    }

    @Test
    void sendTooLongMessage() {
        String content = "가".repeat(ChatManager.MAX_MESSAGE_LENGTH + 1);

        assertThrows(
                BadRequestException.class,
                () -> ChatManager.getInstance().sendMessage(1L, TestFixture.BUYER_ID, content));
    }

    @Test
    void findMissingChatRoom() {
        assertThrows(
                NotFoundException.class,
                () -> ChatManager.getInstance().findChatRoom(-1L, TestFixture.BUYER_ID));
    }

    @Test
    void addSystemMessageWithoutChatRoom() {
        assertThrows(
                NotFoundException.class,
                () ->
                        ChatManager.getInstance()
                                .addSystemMessage(-1L, "SAFE_ZONE_RECORDED", "테스트 Safe Zone"));
    }
}
