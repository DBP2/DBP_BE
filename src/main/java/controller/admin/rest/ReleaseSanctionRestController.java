package controller.admin.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.SanctionManager;
import service.exception.MethodNotAllowedException;

public class ReleaseSanctionRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("POST")) {
            String adminId = UserSessionUtils.getCurrentUserId(request);
            Long sanctionId = JsonUtils.parseId((String) request.getAttribute("sanctionId"));

            SanctionManager.getInstance().releaseSanction(adminId, sanctionId);
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
