package persistence.dao;

import domain.Review;
import java.sql.ResultSet;
import persistence.util.JDBCUtil;

public class ReviewDAO {

    public Long createReview(Review review) throws Exception {
        Long reviewId = null;

        String insertQuery = "INSERT INTO REVIEW (REVIEW_ID, TRADE_ID, WRITER_ID, TARGET_ID, RATING, TAGS, CREATED_AT) "
                           + "SELECT SEQ_REVIEW.NEXTVAL, ?, ?, ?, ?, ?, SYSDATE "
                           + "FROM TRADE "
                           + "WHERE TRADE_ID = ? "
                           + "  AND STATUS = 'COMPLETED' "
                           + "  AND NOT EXISTS (SELECT 1 FROM REVIEW WHERE TRADE_ID = ?)";

        String seqQuery = "SELECT SEQ_REVIEW.NEXTVAL AS REVIEW_ID FROM DUAL";

        JDBCUtil seqUtil = new JDBCUtil(seqQuery, null);
        try {
            ResultSet rs = seqUtil.executeQuery();
            if (rs != null && rs.next()) {
                reviewId = rs.getLong("REVIEW_ID");
            }
            seqUtil.commit();
        } catch (Exception e) {
            seqUtil.rollback();
            throw e;
        } finally {
            seqUtil.close();
        }

        Object[] parameters = new Object[] {
            review.getTradeId(),
            review.getWriterId(),
            review.getTargetId(),
            review.getRating(),
            review.getTagsAsString(),
            review.getTradeId(),
            review.getTradeId()
        };

        JDBCUtil insertUtil = new JDBCUtil(insertQuery, parameters);
        try {
            int affectedRows = insertUtil.executeUpdate();
            
            if (affectedRows == 0) {
                insertUtil.rollback();
                return null; 
            }

            insertUtil.commit();
            review.setReviewId(reviewId);
            return reviewId;
        } catch (Exception e) {
            insertUtil.rollback();
            throw e;
        } finally {
            insertUtil.close();
        }
    }
}