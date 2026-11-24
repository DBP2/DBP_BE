package controller.user.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserManager;
import service.exception.MethodNotAllowedException;

/** POST /rest/login {userId, password} : 성공하면 세션에 "userId" 를 저장한다 */
public class LoginRestController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(LoginRestController.class);

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("POST")) {
            Map<?, ?> body = JsonUtils.readBody(request, Map.class);
            User user =
                    UserManager.getInstance()
                            .login(
                                    UserResponse.toStr(body.get("userId")),
                                    UserResponse.toStr(body.get("password")));

            HttpSession session = request.getSession();
            session.setAttribute(UserSessionUtils.USER_SESSION_KEY, user.getUserId());
            log.debug("Login : {}", user.getUserId());

            request.setAttribute("result", UserResponse.toMeMap(user));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
