package controller.user.rest;

import controller.Controller;
import controller.JsonUtils;
import domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserManager;
import service.exception.BadRequestException;
import service.exception.MethodNotAllowedException;

/**
 * /rest/users
 *
 * <ul>
 *   <li>GET ?userId= 또는 ?nickname= : 사용 가능 여부 {"available": true}
 *   <li>POST : 회원가입 {schoolId, studentNo, studentName, userId, password, nickname}
 * </ul>
 */
public class UserRestController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserManager manager = UserManager.getInstance();

        if (request.getMethod().equals("GET")) {
            String userId = request.getParameter("userId");
            String nickname = request.getParameter("nickname");
            boolean available;
            if (userId != null) {
                available = manager.isUserIdAvailable(userId);
            } else if (nickname != null) {
                available = manager.isNicknameAvailable(nickname);
            } else {
                throw new BadRequestException("userId 또는 nickname 을 보내 주세요");
            }
            request.setAttribute("result", Map.of("available", available));
            return null;
        }

        if (request.getMethod().equals("POST")) {
            Map<?, ?> body = JsonUtils.readBody(request, Map.class);
            User user =
                    manager.signUp(
                            UserResponse.toLong(body.get("schoolId")),
                            UserResponse.toStr(body.get("studentNo")),
                            UserResponse.toStr(body.get("studentName")),
                            UserResponse.toStr(body.get("userId")),
                            UserResponse.toStr(body.get("password")),
                            UserResponse.toStr(body.get("nickname")));
            log.debug("Sign up : {}", user.getUserId());

            request.setAttribute("status", HttpServletResponse.SC_CREATED);
            request.setAttribute("result", UserResponse.toMeMap(user));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
