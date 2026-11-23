package service;

import domain.SafeZone;
import java.sql.SQLException;
import java.util.List;
import persistence.dao.SafeZoneDAO;

public class SafeZoneManager {
    private static SafeZoneManager safeZoneMan = new SafeZoneManager();
    private SafeZoneDAO safeZoneDAO;

    private SafeZoneManager() {
        try {
            safeZoneDAO = new SafeZoneDAO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SafeZoneManager getInstance() {
        return safeZoneMan;
    }

    public List<SafeZone> findSafeZoneList() throws SQLException {
        return safeZoneDAO.getSafeZoneList();
    }
}
