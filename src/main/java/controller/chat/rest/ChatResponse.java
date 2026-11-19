package controller.chat.rest;

import controller.FileUploadUtils;
import controller.JsonUtils;
import domain.ChatMessage;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import service.dto.ChatRoomDetail;
import service.dto.ChatRoomSummary;

public class ChatResponse {

    public static Map<String, Object> toRoomMap(
            HttpServletRequest request, ChatRoomSummary summary) {
        Map<String, Object> partner = new LinkedHashMap<String, Object>();
        partner.put("id", summary.getPartnerId());
        partner.put("name", summary.getPartnerName());
        partner.put("isVerified", summary.isPartnerVerified());

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", JsonUtils.toId(summary.getChatRoomId()));
        map.put("partner", partner);
        map.put("myRole", toLowerCase(summary.getMyRole()));
        map.put("tradeStatus", toLowerCase(summary.getTradeStatus()));
        map.put("postTitle", summary.getPostTitle());
        map.put("thumbnailUrl", FileUploadUtils.toUrl(request, summary.getThumbnailUrl()));
        map.put("lastMessage", summary.getLastMessage());
        map.put("lastMessageAt", JsonUtils.formatDateTime(summary.getLastMessageAt()));
        map.put("unreadCount", summary.getUnreadCount());
        return map;
    }

    public static Map<String, Object> toDetailMap(
            HttpServletRequest request, ChatRoomDetail detail) {
        Map<String, Object> partner = new LinkedHashMap<String, Object>();
        partner.put("id", detail.getPartnerId());
        partner.put("name", detail.getPartnerName());
        partner.put("isVerified", detail.isPartnerVerified());
        partner.put("trustGrade", detail.getPartnerTrustGrade());

        Map<String, Object> post = new LinkedHashMap<String, Object>();
        post.put("id", JsonUtils.toId(detail.getPostId()));
        post.put("title", detail.getPostTitle());
        post.put("price", detail.getPrice());
        post.put("thumbnailUrl", FileUploadUtils.toUrl(request, detail.getThumbnailUrl()));

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", JsonUtils.toId(detail.getChatRoomId()));
        map.put("tradeId", JsonUtils.toId(detail.getTradeId()));
        map.put("tradeStatus", toLowerCase(detail.getTradeStatus()));
        map.put("myRole", toLowerCase(detail.getMyRole()));
        map.put("partner", partner);
        map.put("post", post);
        return map;
    }

    public static Map<String, Object> toMessageMap(
            HttpServletRequest request, ChatMessage message) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", JsonUtils.toId(message.getMessageId()));
        map.put("type", message.getMessageType());
        map.put("createdAt", JsonUtils.formatDateTime(message.getSentAt()));

        if ("SYSTEM".equals(message.getMessageType())) {
            map.put("systemType", message.getSystemType());
            map.put("placeName", message.getPlaceName());
            return map;
        }

        map.put("senderId", message.getSenderId());
        if ("IMAGE".equals(message.getMessageType())) {
            map.put("imageUrl", FileUploadUtils.toUrl(request, message.getImageUrl()));
        } else {
            map.put("content", message.getContent());
        }
        return map;
    }

    private static String toLowerCase(String value) {
        return value == null ? null : value.toLowerCase();
    }
}
