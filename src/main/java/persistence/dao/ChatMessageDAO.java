package persistence.dao;

import domain.ChatMessage;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;

public class ChatMessageDAO {

    private JDBCUtil jdbcUtil = null;

    public ChatMessageDAO() {
        jdbcUtil = new JDBCUtil();
    }

    private static String query =
            "SELECT CHAT_MESSAGE.MESSAGE_ID, "
                    + "CHAT_MESSAGE.CHAT_ROOM_ID, "
                    + "CHAT_MESSAGE.SENDER_ID, "
                    + "CHAT_MESSAGE.MESSAGE_TYPE, "
                    + "CHAT_MESSAGE.CONTENT, "
                    + "CHAT_MESSAGE.IMAGE_URL, "
                    + "CHAT_MESSAGE.SYSTEM_TYPE, "
                    + "CHAT_MESSAGE.PLACE_NAME, "
                    + "CHAT_MESSAGE.SENT_AT, "
                    + "CHAT_MESSAGE.IS_READ "
                    + "FROM CHAT_MESSAGE ";

    public Long insertMessage(ChatMessage message) {
        Long messageId = null;
        String seqQuery = "SELECT SEQ_CHAT_MESSAGE.NEXTVAL AS MESSAGE_ID FROM DUAL ";
        String insertQuery =
                "INSERT INTO CHAT_MESSAGE (MESSAGE_ID, CHAT_ROOM_ID, SENDER_ID, MESSAGE_TYPE, "
                        + "CONTENT, IMAGE_URL, SYSTEM_TYPE, PLACE_NAME) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ";
        String roomQuery =
                "UPDATE CHAT_ROOM SET LAST_MESSAGE_AT = "
                        + "(SELECT SENT_AT FROM CHAT_MESSAGE WHERE MESSAGE_ID = ?) "
                        + "WHERE CHAT_ROOM_ID = ? ";

        try {
            jdbcUtil.setSqlAndParameters(seqQuery, null);
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                messageId = rs.getLong("MESSAGE_ID");
            }

            Object[] param =
                    new Object[] {
                        messageId,
                        message.getChatRoomId(),
                        message.getSenderId(),
                        message.getMessageType(),
                        message.getContent(),
                        message.getImageUrl(),
                        message.getSystemType(),
                        message.getPlaceName()
                    };
            jdbcUtil.setSqlAndParameters(insertQuery, param);
            jdbcUtil.executeUpdate();

            jdbcUtil.setSqlAndParameters(
                    roomQuery, new Object[] {messageId, message.getChatRoomId()});
            jdbcUtil.executeUpdate();

            jdbcUtil.commit();
            message.setMessageId(messageId);
            return messageId;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    public ChatMessage getMessage(Long messageId) {
        String searchQuery = query + "WHERE MESSAGE_ID = ? ";
        Object[] param = new Object[] {messageId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            ChatMessage message = null;
            if (rs.next()) {
                message = toMessage(rs);
            }
            return message;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    public List<ChatMessage> getMessageList(Long chatRoomId) {
        String searchQuery =
                query + "WHERE CHAT_ROOM_ID = ? ORDER BY SENT_AT ASC, MESSAGE_ID ASC ";
        return getMessageListBy(searchQuery, new Object[] {chatRoomId});
    }

    public List<ChatMessage> getMessageListAfter(Long chatRoomId, Long afterMessageId) {
        String searchQuery =
                query
                        + "WHERE CHAT_ROOM_ID = ? AND MESSAGE_ID > ? "
                        + "ORDER BY SENT_AT ASC, MESSAGE_ID ASC ";
        return getMessageListBy(searchQuery, new Object[] {chatRoomId, afterMessageId});
    }

    public int updateRead(Long chatRoomId, String userId) {
        String updateQuery =
                "UPDATE CHAT_MESSAGE SET IS_READ = 'Y' "
                        + "WHERE CHAT_ROOM_ID = ? AND IS_READ = 'N' "
                        + "AND (SENDER_ID IS NULL OR SENDER_ID <> ?) ";
        Object[] param = new Object[] {chatRoomId, userId};
        jdbcUtil.setSqlAndParameters(updateQuery, param);

        try {
            int result = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return result;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return 0;
    }

    public int getUnreadCount(Long chatRoomId, String userId) {
        String searchQuery =
                "SELECT COUNT(*) AS CNT FROM CHAT_MESSAGE "
                        + "WHERE CHAT_ROOM_ID = ? AND IS_READ = 'N' "
                        + "AND (SENDER_ID IS NULL OR SENDER_ID <> ?) ";
        Object[] param = new Object[] {chatRoomId, userId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                return rs.getInt("CNT");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return 0;
    }

    private List<ChatMessage> getMessageListBy(String searchQuery, Object[] param) {
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            List<ChatMessage> list = new ArrayList<ChatMessage>();
            while (rs.next()) {
                list.add(toMessage(rs));
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private ChatMessage toMessage(ResultSet rs) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setMessageId(rs.getLong("MESSAGE_ID"));
        message.setChatRoomId(rs.getLong("CHAT_ROOM_ID"));
        message.setSenderId(rs.getString("SENDER_ID"));
        message.setMessageType(rs.getString("MESSAGE_TYPE"));
        message.setContent(rs.getString("CONTENT"));
        message.setImageUrl(rs.getString("IMAGE_URL"));
        message.setSystemType(rs.getString("SYSTEM_TYPE"));
        message.setPlaceName(rs.getString("PLACE_NAME"));
        Timestamp sentAt = rs.getTimestamp("SENT_AT");
        if (sentAt != null) {
            message.setSentAt(sentAt.toLocalDateTime());
        }
        message.setRead("Y".equals(rs.getString("IS_READ")));
        return message;
    }
}
