package controller.admin.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import controller.user.rest.UserResponse;
import domain.Sanction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.SanctionManager;
import service.exception.MethodNotAllowedException;

/**
 * POST /rest/admin/sanctions {targetId, type, reason, days, reportId} : 제재 적용 (관리자, 201)
 *
 * <p>type: WARNING(경고) / SUSPENSION(기간 정지, days 필수) / BAN(영구 정지)
 */
public class SanctionRestController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(SanctionRestController.class);

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("POST")) {
            String adminId = UserSessionUtils.getCurrentUserId(request);
            Map<?, ?> body = JsonUtils.readBody(request, Map.class);
            Long days = UserResponse.toLong(body.get("days"));

            Sanction sanction =
                    SanctionManager.getInstance()
                            .applySanction(
                                    adminId,
                                    UserResponse.toStr(body.get("targetId")),
                                    UserResponse.toStr(body.get("type")),
                                    UserResponse.toStr(body.get("reason")),
                                    days == null ? null : days.intValue(),
                                    UserResponse.toLong(body.get("reportId")));
            log.debug("Apply Sanction : {}", sanction);

            request.setAttribute("status", HttpServletResponse.SC_CREATED);
            request.setAttribute("result", SanctionResponse.toSanctionMap(sanction));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
