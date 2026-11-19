package controller.safezone.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.SafeZone;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.SafeZoneManager;
import service.exception.MethodNotAllowedException;

public class VerifySafeZoneRestController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(VerifySafeZoneRestController.class);

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("POST")) {
            String tradeId = (String) request.getAttribute("tradeId");
            String userId = UserSessionUtils.getCurrentUserId(request);

            String jsonData = request.getReader().readLine();
            log.debug("input data: {}", jsonData);

            ObjectMapper objMapper = JsonUtils.getMapper();
            Map<?, ?> body = objMapper.readValue(jsonData, Map.class);

            SafeZone safeZone =
                    SafeZoneManager.getInstance()
                            .verifySafeZone(
                                    JsonUtils.parseId(tradeId),
                                    userId,
                                    (String) body.get("type"),
                                    (String) body.get("value"));

            request.setAttribute("result", TradeRecordResponse.toVerificationMap(safeZone));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
