package controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import service.exception.UnauthorizedException;

public class UserSessionUtils {
    public static final String USER_SESSION_KEY = "userId";

    public static String getLoginUserId(HttpSession session) {
        String userId = (String) session.getAttribute(USER_SESSION_KEY);
        return userId;
    }

    public static boolean hasLogined(HttpSession session) {
        if (getLoginUserId(session) != null) {
            return true;
        }
        return false;
    }

    public static boolean isLoginUser(String userId, HttpSession session) {
        String loginUser = getLoginUserId(session);
        if (loginUser == null) {
            return false;
        }
        return loginUser.equals(userId);
    }

    public static String getCurrentUserId(HttpServletRequest request) throws UnauthorizedException {
        HttpSession session = request.getSession(false);
        if (session != null && hasLogined(session)) {
            return getLoginUserId(session);
        }
        throw new UnauthorizedException("로그인이 필요해요");
    }
}
