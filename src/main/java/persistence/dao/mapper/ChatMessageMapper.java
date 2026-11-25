package persistence.dao.mapper;

import domain.ChatMessage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ChatMessageMapper {
    Long selectNextMessageId();

    int insertMessage(ChatMessage message);

    int updateLastMessageAt(ChatMessage message);

    ChatMessage selectMessage(Long messageId);

    List<ChatMessage> selectMessageList(Long chatRoomId);

    List<ChatMessage> selectMessageListAfter(
            @Param("chatRoomId") Long chatRoomId, @Param("afterMessageId") Long afterMessageId);

    int updateRead(@Param("chatRoomId") Long chatRoomId, @Param("userId") String userId);

    int selectUnreadCount(@Param("chatRoomId") Long chatRoomId, @Param("userId") String userId);
}
