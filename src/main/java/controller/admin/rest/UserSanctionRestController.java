package controller.admin.rest;

import controller.Controller;
import controller.UserSessionUtils;
import domain.Sanction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import service.SanctionManager;
import service.exception.MethodNotAllowedException;

/** GET /rest/admin/users/{userId}/sanctions : 한 회원의 제재 이력 (관리자) */
public class UserSanctionRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("GET")) {
            String adminId = UserSessionUtils.getCurrentUserId(request);
            String userId = (String) request.getAttribute("userId");

            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (Sanction sanction :
                    SanctionManager.getInstance().findSanctionList(adminId, userId)) {
                result.add(SanctionResponse.toSanctionMap(sanction));
            }
            request.setAttribute("result", result);
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
