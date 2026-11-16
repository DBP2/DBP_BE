package persistence.dao;

import domain.ChatRoom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import persistence.util.JDBCUtil;
import service.dto.ChatRoomDetail;
import service.dto.ChatRoomSummary;

public class ChatRoomDAO {

    private JDBCUtil jdbcUtil = null;

    public ChatRoomDAO() {
        jdbcUtil = new JDBCUtil();
    }

    private static String query =
            "SELECT CHAT_ROOM.CHAT_ROOM_ID, "
                    + "CHAT_ROOM.POST_ID, "
                    + "CHAT_ROOM.BUYER_ID, "
                    + "CHAT_ROOM.TRADE_ID, "
                    + "CHAT_ROOM.CREATED_AT, "
                    + "CHAT_ROOM.LAST_MESSAGE_AT "
                    + "FROM CHAT_ROOM ";

    private static String partnerQuery =
            "FROM CHAT_ROOM r "
                    + "JOIN POST p ON r.POST_ID = p.POST_ID "
                    + "JOIN MEMBER m ON m.USER_ID = "
                    + "CASE WHEN r.BUYER_ID = ? THEN p.WRITER_ID ELSE r.BUYER_ID END "
                    + "LEFT JOIN TRADE t ON r.TRADE_ID = t.TRADE_ID ";

    public Long insertChatRoom(ChatRoom room) {
        Long chatRoomId = null;
        String seqQuery = "SELECT SEQ_CHAT_ROOM.NEXTVAL AS CHAT_ROOM_ID FROM DUAL ";
        String insertQuery =
                "INSERT INTO CHAT_ROOM (CHAT_ROOM_ID, POST_ID, BUYER_ID, TRADE_ID) "
                        + "VALUES (?, ?, ?, ?) ";

        try {
            jdbcUtil.setSqlAndParameters(seqQuery, null);
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                chatRoomId = rs.getLong("CHAT_ROOM_ID");
            }

            Object[] param =
                    new Object[] {
                        chatRoomId, room.getPostId(), room.getBuyerId(), room.getTradeId()
                    };
            jdbcUtil.setSqlAndParameters(insertQuery, param);
            jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            room.setChatRoomId(chatRoomId);
            return chatRoomId;
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            if (ex.getErrorCode() == 1) {
                System.out.println("이미 같은 게시글에 대한 채팅방이 존재합니다.");
            } else {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            jdbcUtil.rollback();
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    public ChatRoom getChatRoom(Long chatRoomId) {
        String searchQuery = query + "WHERE CHAT_ROOM_ID = ? ";
        return getChatRoomBy(searchQuery, new Object[] {chatRoomId});
    }

    public ChatRoom getChatRoomByPostAndBuyer(Long postId, String buyerId) {
        String searchQuery = query + "WHERE POST_ID = ? AND BUYER_ID = ? ";
        return getChatRoomBy(searchQuery, new Object[] {postId, buyerId});
    }

    public ChatRoom getChatRoomByTrade(Long tradeId) {
        String searchQuery = query + "WHERE TRADE_ID = ? ";
        return getChatRoomBy(searchQuery, new Object[] {tradeId});
    }

    public int updateTrade(Long chatRoomId, Long tradeId) {
        String updateQuery = "UPDATE CHAT_ROOM SET TRADE_ID = ? WHERE CHAT_ROOM_ID = ? ";
        return executeUpdate(updateQuery, new Object[] {tradeId, chatRoomId});
    }

    public int updateLastMessageAt(Long chatRoomId) {
        String updateQuery =
                "UPDATE CHAT_ROOM SET LAST_MESSAGE_AT = SYSDATE WHERE CHAT_ROOM_ID = ? ";
        return executeUpdate(updateQuery, new Object[] {chatRoomId});
    }

    public boolean isParticipant(Long chatRoomId, String userId) {
        String searchQuery =
                "SELECT COUNT(*) AS CNT FROM CHAT_ROOM r "
                        + "JOIN POST p ON r.POST_ID = p.POST_ID "
                        + "WHERE r.CHAT_ROOM_ID = ? AND (r.BUYER_ID = ? OR p.WRITER_ID = ?) ";
        Object[] param = new Object[] {chatRoomId, userId, userId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                return rs.getInt("CNT") > 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return false;
    }

    public List<ChatRoomSummary> getChatRoomSummaryList(String userId) {
        String searchQuery =
                "SELECT r.CHAT_ROOM_ID, "
                        + "CASE WHEN r.BUYER_ID = ? THEN 'BUYER' ELSE 'SELLER' END AS MY_ROLE, "
                        + "m.USER_ID AS PARTNER_ID, "
                        + "m.NICKNAME AS PARTNER_NAME, "
                        + "CASE WHEN m.STUDENT_ID IS NOT NULL THEN 'Y' ELSE 'N' END "
                        + "AS PARTNER_VERIFIED, "
                        + "t.STATUS AS TRADE_STATUS, "
                        + "p.TITLE AS POST_TITLE, "
                        + "(SELECT i.URL FROM POST_IMAGE i "
                        + "WHERE i.POST_ID = p.POST_ID AND i.IMAGE_ORDER = 0) AS THUMBNAIL_URL, "
                        + "(SELECT MAX(c.MESSAGE_TYPE) "
                        + "KEEP (DENSE_RANK LAST ORDER BY c.SENT_AT, c.MESSAGE_ID) "
                        + "FROM CHAT_MESSAGE c WHERE c.CHAT_ROOM_ID = r.CHAT_ROOM_ID) "
                        + "AS LAST_MESSAGE_TYPE, "
                        + "(SELECT MAX(c.CONTENT) "
                        + "KEEP (DENSE_RANK LAST ORDER BY c.SENT_AT, c.MESSAGE_ID) "
                        + "FROM CHAT_MESSAGE c WHERE c.CHAT_ROOM_ID = r.CHAT_ROOM_ID) "
                        + "AS LAST_MESSAGE, "
                        + "r.LAST_MESSAGE_AT, "
                        + "(SELECT COUNT(*) FROM CHAT_MESSAGE c "
                        + "WHERE c.CHAT_ROOM_ID = r.CHAT_ROOM_ID AND c.IS_READ = 'N' "
                        + "AND (c.SENDER_ID IS NULL OR c.SENDER_ID <> ?)) AS UNREAD_COUNT "
                        + partnerQuery
                        + "WHERE r.BUYER_ID = ? OR p.WRITER_ID = ? "
                        + "ORDER BY NVL(r.LAST_MESSAGE_AT, r.CREATED_AT) DESC ";
        Object[] param = new Object[] {userId, userId, userId, userId, userId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            List<ChatRoomSummary> list = new ArrayList<ChatRoomSummary>();
            while (rs.next()) {
                ChatRoomSummary summary = new ChatRoomSummary();
                summary.setChatRoomId(rs.getLong("CHAT_ROOM_ID"));
                summary.setMyRole(rs.getString("MY_ROLE"));
                summary.setPartnerId(rs.getString("PARTNER_ID"));
                summary.setPartnerName(rs.getString("PARTNER_NAME"));
                summary.setPartnerVerified("Y".equals(rs.getString("PARTNER_VERIFIED")));
                summary.setTradeStatus(rs.getString("TRADE_STATUS"));
                summary.setPostTitle(rs.getString("POST_TITLE"));
                summary.setThumbnailUrl(rs.getString("THUMBNAIL_URL"));
                summary.setLastMessageType(rs.getString("LAST_MESSAGE_TYPE"));
                summary.setLastMessage(rs.getString("LAST_MESSAGE"));
                summary.setLastMessageAt(toLocalDateTime(rs.getTimestamp("LAST_MESSAGE_AT")));
                summary.setUnreadCount(rs.getInt("UNREAD_COUNT"));
                list.add(summary);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    public ChatRoomDetail getChatRoomDetail(Long chatRoomId, String userId) {
        String searchQuery =
                "SELECT r.CHAT_ROOM_ID, "
                        + "r.TRADE_ID, "
                        + "t.STATUS AS TRADE_STATUS, "
                        + "CASE WHEN r.BUYER_ID = ? THEN 'BUYER' ELSE 'SELLER' END AS MY_ROLE, "
                        + "m.USER_ID AS PARTNER_ID, "
                        + "m.NICKNAME AS PARTNER_NAME, "
                        + "CASE WHEN m.STUDENT_ID IS NOT NULL THEN 'Y' ELSE 'N' END "
                        + "AS PARTNER_VERIFIED, "
                        + "m.TRUST_SCORE AS PARTNER_TRUST_SCORE, "
                        + "p.POST_ID, "
                        + "p.TITLE AS POST_TITLE, "
                        + "p.PRICE, "
                        + "(SELECT i.URL FROM POST_IMAGE i "
                        + "WHERE i.POST_ID = p.POST_ID AND i.IMAGE_ORDER = 0) AS THUMBNAIL_URL "
                        + partnerQuery
                        + "WHERE r.CHAT_ROOM_ID = ? AND (r.BUYER_ID = ? OR p.WRITER_ID = ?) ";
        Object[] param = new Object[] {userId, userId, chatRoomId, userId, userId};
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            ChatRoomDetail detail = null;
            if (rs.next()) {
                detail = new ChatRoomDetail();
                detail.setChatRoomId(rs.getLong("CHAT_ROOM_ID"));
                long tradeId = rs.getLong("TRADE_ID");
                detail.setTradeId(rs.wasNull() ? null : tradeId);
                detail.setTradeStatus(rs.getString("TRADE_STATUS"));
                detail.setMyRole(rs.getString("MY_ROLE"));
                detail.setPartnerId(rs.getString("PARTNER_ID"));
                detail.setPartnerName(rs.getString("PARTNER_NAME"));
                detail.setPartnerVerified("Y".equals(rs.getString("PARTNER_VERIFIED")));
                detail.setPartnerTrustScore(rs.getInt("PARTNER_TRUST_SCORE"));
                detail.setPostId(rs.getLong("POST_ID"));
                detail.setPostTitle(rs.getString("POST_TITLE"));
                detail.setPrice(rs.getInt("PRICE"));
                detail.setThumbnailUrl(rs.getString("THUMBNAIL_URL"));
            }
            return detail;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private ChatRoom getChatRoomBy(String searchQuery, Object[] param) {
        jdbcUtil.setSqlAndParameters(searchQuery, param);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            ChatRoom room = null;
            if (rs.next()) {
                room = new ChatRoom();
                room.setChatRoomId(rs.getLong("CHAT_ROOM_ID"));
                room.setPostId(rs.getLong("POST_ID"));
                room.setBuyerId(rs.getString("BUYER_ID"));
                long tradeId = rs.getLong("TRADE_ID");
                room.setTradeId(rs.wasNull() ? null : tradeId);
                room.setCreatedAt(toLocalDateTime(rs.getTimestamp("CREATED_AT")));
                room.setLastMessageAt(toLocalDateTime(rs.getTimestamp("LAST_MESSAGE_AT")));
            }
            return room;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private int executeUpdate(String updateQuery, Object[] param) {
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

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
