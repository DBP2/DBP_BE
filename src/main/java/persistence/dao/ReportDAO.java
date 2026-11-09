package persistence.dao;

import domain.Report;
import java.sql.ResultSet;
import persistence.util.JDBCUtil;

public class ReportDAO {

    public Long createReport(Report report) throws Exception {
        if (report.getReporterId() != null && report.getReporterId().equals(report.getTargetId())) {
            return null; 
        }

        Long reportId = null;

        String seqQuery = "SELECT SEQ_REPORT.NEXTVAL AS REPORT_ID FROM DUAL";
        
        String insertQuery = "INSERT INTO REPORT (REPORT_ID, TYPE, REASON, CREATED_AT, TARGET_ID, REPORTER_ID, TRADE_ID) "
                           + "VALUES (?, ?, ?, SYSDATE, ?, ?, ?)";

        JDBCUtil seqUtil = new JDBCUtil(seqQuery, null);
        try {
            ResultSet rs = seqUtil.executeQuery();
            if (rs != null && rs.next()) {
                reportId = rs.getLong("REPORT_ID");
            }
            seqUtil.commit();
        } catch (Exception e) {
            seqUtil.rollback();
            throw e;
        } finally {
            seqUtil.close();
        }

        Object[] parameters = new Object[] {
            reportId,
            report.getType(),
            report.getReason(),
            report.getTargetId(),
            report.getReporterId(),
            report.getTradeId()
        };

        JDBCUtil insertUtil = new JDBCUtil(insertQuery, parameters);
        try {
            int affectedRows = insertUtil.executeUpdate();
            
            if (affectedRows == 0) {
                insertUtil.rollback();
                return null;
            }

            insertUtil.commit();
            report.setReportId(reportId);
            return reportId;
        } catch (Exception e) {
            insertUtil.rollback();
            throw e;
        } finally {
            insertUtil.close();
        }
    }
}