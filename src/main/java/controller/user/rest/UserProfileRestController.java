package controller.user.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import service.UserManager;
import service.exception.MethodNotAllowedException;

/**
 * /rest/users/{userId}. "/rest/users/me" 도 이 주소 형식에 걸리므로 여기서 함께 처리한다.
 *
 * <ul>
 *   <li>GET /rest/users/me : 내 정보 {id, name, ...}
 *   <li>PUT /rest/users/me : 내 프로필 수정 {nickname, introduction}
 *   <li>GET /rest/users/{userId} : 다른 회원의 공개 프로필
 * </ul>
 */
public class UserProfileRestController implements Controller {
    public static final String ME = "me";

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        UserManager manager = UserManager.getInstance();
        String userId = (String) request.getAttribute("userId");

        if (ME.equals(userId)) {
            String loginUserId = UserSessionUtils.getCurrentUserId(request);

            if (request.getMethod().equals("GET")) {
                User me = manager.findUser(loginUserId);
                request.setAttribute("result", UserResponse.toMeMap(me));
                return null;
            }

            if (request.getMethod().equals("PUT")) {
                Map<?, ?> body = JsonUtils.readBody(request, Map.class);
                User me =
                        manager.updateProfile(
                                loginUserId,
                                UserResponse.toStr(body.get("nickname")),
                                UserResponse.toStr(body.get("introduction")));
                request.setAttribute("result", UserResponse.toMeMap(me));
                return null;
            }
        } else if (request.getMethod().equals("GET")) {
            User user = manager.findUser(userId);
            request.setAttribute("result", UserResponse.toProfileMap(user));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
