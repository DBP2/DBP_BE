package service;

import domain.ChatMessage;
import domain.ChatRoom;
import domain.User;
import java.sql.SQLException;
import java.util.List;
import persistence.dao.ChatMessageDAO;
import persistence.dao.ChatRoomDAO;
import service.dto.ChatRoomDetail;
import service.dto.ChatRoomSummary;
import service.exception.BadRequestException;
import service.exception.ForbiddenException;
import service.exception.NotFoundException;

public class ChatManager {
    public static final int MAX_MESSAGE_LENGTH = 1000;

    private static ChatManager chatMan = new ChatManager();
    private ChatRoomDAO chatRoomDAO;
    private ChatMessageDAO chatMessageDAO;

    private ChatManager() {
        try {
            chatRoomDAO = new ChatRoomDAO();
            chatMessageDAO = new ChatMessageDAO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ChatManager getInstance() {
        return chatMan;
    }

    public Long openChatRoom(Long postId, String userId)
            throws SQLException, NotFoundException, BadRequestException {
        String writerId = chatRoomDAO.getPostWriterId(postId);
        if (writerId == null) {
            throw new NotFoundException("게시글을 찾을 수 없어요");
        }
        if (writerId.equals(userId)) {
            throw new BadRequestException("내 게시글에는 채팅할 수 없어요");
        }

        ChatRoom room = chatRoomDAO.getChatRoomByPostAndBuyer(postId, userId);
        if (room != null) {
            return room.getChatRoomId();
        }

        room = new ChatRoom();
        room.setPostId(postId);
        room.setBuyerId(userId);
        Long chatRoomId = chatRoomDAO.insertChatRoom(room);
        if (chatRoomId == null) {
            throw new SQLException("채팅방을 만들지 못했어요");
        }
        return chatRoomId;
    }

    public List<ChatRoomSummary> findChatRoomList(String userId) throws SQLException {
        List<ChatRoomSummary> roomList = chatRoomDAO.getChatRoomSummaryList(userId);
        for (ChatRoomSummary summary : roomList) {
            if ("IMAGE".equals(summary.getLastMessageType())) {
                summary.setLastMessage("사진을 보냈어요");
            } else if ("SYSTEM".equals(summary.getLastMessageType())) {
                summary.setLastMessage("거래 알림이 도착했어요");
            } else if (summary.getLastMessage() == null) {
                summary.setLastMessage("");
            }
        }
        return roomList;
    }

    public ChatRoomDetail findChatRoom(Long chatRoomId, String userId)
            throws SQLException, NotFoundException, ForbiddenException {
        checkParticipant(chatRoomId, userId);

        ChatRoomDetail detail = chatRoomDAO.getChatRoomDetail(chatRoomId, userId);
        detail.setPartnerTrustGrade(getTrustGrade(detail.getPartnerTrustScore()));
        return detail;
    }

    public List<ChatMessage> findMessageList(Long chatRoomId, String userId, Long afterMessageId)
            throws SQLException, NotFoundException, ForbiddenException {
        checkParticipant(chatRoomId, userId);

        if (afterMessageId == null) {
            return chatMessageDAO.getMessageList(chatRoomId);
        }
        return chatMessageDAO.getMessageListAfter(chatRoomId, afterMessageId);
    }

    public ChatMessage sendMessage(Long chatRoomId, String userId, String content)
            throws SQLException, NotFoundException, ForbiddenException, BadRequestException {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("메시지를 입력해 주세요");
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new BadRequestException("메시지는 1000자까지 보낼 수 있어요");
        }
        checkParticipant(chatRoomId, userId);

        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setSenderId(userId);
        message.setMessageType("TEXT");
        message.setContent(content);
        return createMessage(message);
    }

    public ChatMessage sendImage(Long chatRoomId, String userId, String imageUrl)
            throws SQLException, NotFoundException, ForbiddenException {
        checkParticipant(chatRoomId, userId);

        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setSenderId(userId);
        message.setMessageType("IMAGE");
        message.setImageUrl(imageUrl);
        return createMessage(message);
    }

    public int readMessages(Long chatRoomId, String userId)
            throws SQLException, NotFoundException, ForbiddenException {
        checkParticipant(chatRoomId, userId);

        return chatMessageDAO.updateRead(chatRoomId, userId);
    }

    public ChatMessage addSystemMessage(Long tradeId, String systemType, String placeName)
            throws SQLException, NotFoundException {
        ChatRoom room = chatRoomDAO.getChatRoomByTrade(tradeId);
        if (room == null) {
            throw new NotFoundException("거래 채팅방을 찾을 수 없어요");
        }

        ChatMessage message = new ChatMessage();
        message.setChatRoomId(room.getChatRoomId());
        message.setMessageType("SYSTEM");
        message.setSystemType(systemType);
        message.setPlaceName(placeName);
        return createMessage(message);
    }

    public void checkParticipant(Long chatRoomId, String userId)
            throws SQLException, NotFoundException, ForbiddenException {
        if (chatRoomDAO.getChatRoom(chatRoomId) == null) {
            throw new NotFoundException("채팅방을 찾을 수 없어요");
        }
        if (chatRoomDAO.isParticipant(chatRoomId, userId) == false) {
            throw new ForbiddenException("참여하지 않은 채팅방이에요");
        }
    }

    public String getTrustGrade(int trustScore) {
        User user = new User();
        user.setTrustScore(trustScore);
        return user.getTrustGrade();
    }

    private ChatMessage createMessage(ChatMessage message) throws SQLException {
        Long messageId = chatMessageDAO.insertMessage(message);
        if (messageId == null) {
            throw new SQLException("메시지를 저장하지 못했어요");
        }
        return chatMessageDAO.getMessage(messageId);
    }
}
