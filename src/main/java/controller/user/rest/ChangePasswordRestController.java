package controller.user.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import service.UserManager;
import service.exception.MethodNotAllowedException;

/** PUT /rest/users/me/password {currentPassword, newPassword} : 비밀번호 변경 (204) */
public class ChangePasswordRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("PUT")) {
            String userId = UserSessionUtils.getCurrentUserId(request);
            Map<?, ?> body = JsonUtils.readBody(request, Map.class);

            UserManager.getInstance()
                    .changePassword(
                            userId,
                            UserResponse.toStr(body.get("currentPassword")),
                            UserResponse.toStr(body.get("newPassword")));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
