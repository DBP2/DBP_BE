package controller.user.rest;

import controller.Controller;
import controller.UserSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.exception.MethodNotAllowedException;

/** POST /rest/logout : 세션을 지운다 (204) */
public class LogoutRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("POST")) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(UserSessionUtils.USER_SESSION_KEY);
                session.invalidate();
            }
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
