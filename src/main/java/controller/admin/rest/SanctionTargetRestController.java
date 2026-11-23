package controller.admin.rest;

import controller.Controller;
import controller.UserSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import service.SanctionManager;
import service.dto.SanctionTarget;
import service.exception.MethodNotAllowedException;

/** GET /rest/admin/sanction-targets?filter=pending|sanctioned|all : 제재 대상 목록 (관리자) */
public class SanctionTargetRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("GET")) {
            String adminId = UserSessionUtils.getCurrentUserId(request);
            List<SanctionTarget> targetList =
                    SanctionManager.getInstance()
                            .findTargetList(adminId, request.getParameter("filter"));

            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (SanctionTarget target : targetList) {
                result.add(SanctionResponse.toTargetMap(target));
            }
            request.setAttribute("result", result);
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
