package controller.chat.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import domain.ChatMessage;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import service.dto.ChatRoomDetail;

class ChatResponseTest {

    private HttpServletRequest request =
            (HttpServletRequest)
                    Proxy.newProxyInstance(
                            getClass().getClassLoader(),
                            new Class<?>[] {HttpServletRequest.class},
                            (proxy, method, args) ->
                                    switch (method.getName()) {
                                        case "getScheme" -> "http";
                                        case "getServerName" -> "localhost";
                                        case "getServerPort" -> 8080;
                                        case "getContextPath" -> "/campusmarket";
                                        default -> null;
                                    });

    @Test
    void toTextMessageMap() {
        ChatMessage message = new ChatMessage();
        message.setMessageId(10L);
        message.setSenderId("test_buyer");
        message.setMessageType("TEXT");
        message.setContent("안녕하세요");
        message.setSentAt(LocalDateTime.of(2026, 11, 17, 14, 30));

        Map<String, Object> map = ChatResponse.toMessageMap(request, message);

        assertEquals("10", map.get("id"));
        assertEquals("TEXT", map.get("type"));
        assertEquals("test_buyer", map.get("senderId"));
        assertEquals("안녕하세요", map.get("content"));
        assertEquals("2026-11-17T14:30:00+09:00", map.get("createdAt"));
    }

    @Test
    void toImageMessageMap() {
        ChatMessage message = new ChatMessage();
        message.setMessageId(11L);
        message.setSenderId("test_buyer");
        message.setMessageType("IMAGE");
        message.setImageUrl("/uploads/chat/a.jpg");

        Map<String, Object> map = ChatResponse.toMessageMap(request, message);

        assertEquals("http://localhost:8080/campusmarket/uploads/chat/a.jpg", map.get("imageUrl"));
        assertFalse(map.containsKey("content"));
    }

    @Test
    void toSystemMessageMap() {
        ChatMessage message = new ChatMessage();
        message.setMessageId(12L);
        message.setMessageType("SYSTEM");
        message.setSystemType("SAFE_ZONE_RECORDED");
        message.setPlaceName("숭인관 Safe Zone");

        Map<String, Object> map = ChatResponse.toMessageMap(request, message);

        assertEquals("SAFE_ZONE_RECORDED", map.get("systemType"));
        assertEquals("숭인관 Safe Zone", map.get("placeName"));
        assertFalse(map.containsKey("senderId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toDetailMap() {
        ChatRoomDetail detail = new ChatRoomDetail();
        detail.setChatRoomId(1L);
        detail.setMyRole("BUYER");
        detail.setPartnerId("test_seller");
        detail.setPartnerName("테스트판매자");
        detail.setPartnerVerified(true);
        detail.setPartnerTrustGrade("A");
        detail.setPostId(3L);
        detail.setPostTitle("데이터베이스 개론");
        detail.setPrice(9000);

        Map<String, Object> map = ChatResponse.toDetailMap(request, detail);
        Map<String, Object> partner = (Map<String, Object>) map.get("partner");
        Map<String, Object> post = (Map<String, Object>) map.get("post");

        assertEquals("1", map.get("id"));
        assertNull(map.get("tradeId"));
        assertNull(map.get("tradeStatus"));
        assertEquals("buyer", map.get("myRole"));
        assertEquals("A", partner.get("trustGrade"));
        assertEquals("3", post.get("id"));
        assertEquals(9000, post.get("price"));
    }
}
