package controller.safezone.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.SafeZoneManager;
import service.dto.TradeRecordInfo;
import service.exception.MethodNotAllowedException;

public class ViewTradeRecordInfoRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("GET")) {
            String tradeId = (String) request.getAttribute("tradeId");
            String userId = UserSessionUtils.getCurrentUserId(request);

            TradeRecordInfo info =
                    SafeZoneManager.getInstance()
                            .findTradeRecordInfo(JsonUtils.parseId(tradeId), userId);

            request.setAttribute("result", TradeRecordResponse.toInfoMap(info));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
