package controller.safezone.rest;

import controller.Controller;
import controller.DispatcherServlet;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.SafeZoneRecord;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.SafeZoneManager;
import service.exception.MethodNotAllowedException;

public class ViewMyTradeRecordRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("GET")) {
            String tradeId = (String) request.getAttribute("tradeId");
            String userId = UserSessionUtils.getCurrentUserId(request);

            SafeZoneRecord record =
                    SafeZoneManager.getInstance().findMyRecord(JsonUtils.parseId(tradeId), userId);

            if (record == null) {
                request.setAttribute("result", DispatcherServlet.NULL_RESULT);
            } else {
                request.setAttribute("result", TradeRecordResponse.toRecordMap(request, record));
            }
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
