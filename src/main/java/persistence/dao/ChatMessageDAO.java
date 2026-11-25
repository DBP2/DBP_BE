package persistence.dao;

import domain.ChatMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import persistence.dao.mapper.ChatMessageMapper;

public class ChatMessageDAO {

    private SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();

    private SqlSessionFactory createSqlSessionFactory() {
        String resource = "mybatis/config/mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return new SqlSessionFactoryBuilder().build(inputStream);
    }

    public Long insertMessage(ChatMessage message) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Long messageId = sqlSession.getMapper(ChatMessageMapper.class).selectNextMessageId();
            message.setMessageId(messageId);
            sqlSession.getMapper(ChatMessageMapper.class).insertMessage(message);
            sqlSession.getMapper(ChatMessageMapper.class).updateLastMessageAt(message);
            sqlSession.commit();
            return messageId;
        } catch (Exception e) {
            sqlSession.rollback();
            e.printStackTrace();
            return null;
        } finally {
            sqlSession.close();
        }
    }

    public ChatMessage getMessage(Long messageId) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            return sqlSession.getMapper(ChatMessageMapper.class).selectMessage(messageId);
        } finally {
            sqlSession.close();
        }
    }

    public List<ChatMessage> getMessageList(Long chatRoomId) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            return sqlSession.getMapper(ChatMessageMapper.class).selectMessageList(chatRoomId);
        } finally {
            sqlSession.close();
        }
    }

    public List<ChatMessage> getMessageListAfter(Long chatRoomId, Long afterMessageId) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            return sqlSession
                    .getMapper(ChatMessageMapper.class)
                    .selectMessageListAfter(chatRoomId, afterMessageId);
        } finally {
            sqlSession.close();
        }
    }

    public int updateRead(Long chatRoomId, String userId) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            int result =
                    sqlSession.getMapper(ChatMessageMapper.class).updateRead(chatRoomId, userId);
            sqlSession.commit();
            return result;
        } catch (Exception e) {
            sqlSession.rollback();
            e.printStackTrace();
            return 0;
        } finally {
            sqlSession.close();
        }
    }

    public int getUnreadCount(Long chatRoomId, String userId) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            return sqlSession
                    .getMapper(ChatMessageMapper.class)
                    .selectUnreadCount(chatRoomId, userId);
        } finally {
            sqlSession.close();
        }
    }
}
