package persistence.dao;

import persistence.util.JDBCUtil;

public class TradeDAO {

    public int applyTrade(Long tradeId, String buyerId) throws Exception {
        String sql = "UPDATE TRADE SET BUYER_ID = ?, STATUS = 'REQUESTED', UPDATE_DATE = SYSDATE "
                   + "WHERE TRADE_ID = ? AND BUYER_ID IS NULL AND SELLER_ID <> ?";
        Object[] parameters = new Object[] { buyerId, tradeId, buyerId };
        
        JDBCUtil util = new JDBCUtil(sql, parameters);
        try {
            int result = util.executeUpdate();
            util.commit();
            return result;
        } catch (Exception e) {
            util.rollback();
            throw e;
        } finally {
            util.close();
        }
    }

    public int acceptTrade(Long tradeId) throws Exception {
        String sql = "UPDATE TRADE SET STATUS = 'ACCEPTED', UPDATE_DATE = SYSDATE "
                   + "WHERE TRADE_ID = ? AND STATUS = 'REQUESTED'";
        Object[] parameters = new Object[] { tradeId };
        
        JDBCUtil util = new JDBCUtil(sql, parameters);
        try {
            int result = util.executeUpdate();
            util.commit();
            return result;
        } catch (Exception e) {
            util.rollback();
            throw e;
        } finally {
            util.close();
        }
    }

    public int completeTrade(Long tradeId) throws Exception {
        String sql = "UPDATE TRADE SET STATUS = 'COMPLETED', UPDATE_DATE = SYSDATE "
                   + "WHERE TRADE_ID = ? AND STATUS = 'ACCEPTED'";
        Object[] parameters = new Object[] { tradeId };
        
        JDBCUtil util = new JDBCUtil(sql, parameters);
        try {
            int result = util.executeUpdate();
            util.commit();
            return result;
        } catch (Exception e) {
            util.rollback();
            throw e;
        } finally {
            util.close();
        }
    }
    
    public int cancelTrade(Long tradeId) throws Exception {
        String sql = "DELETE FROM TRADE WHERE TRADE_ID = ? AND STATUS = 'REQUESTED'";
        Object[] parameters = new Object[] { tradeId };
        
        JDBCUtil util = new JDBCUtil(sql, parameters);
        try {
            int result = util.executeUpdate();
            util.commit();
            return result;
        } catch (Exception e) {
            util.rollback();
            throw e;
        } finally {
            util.close();
        }
    }
}