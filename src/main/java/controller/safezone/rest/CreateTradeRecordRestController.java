package controller.safezone.rest;

import controller.Controller;
import controller.FileUploadUtils;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.SafeZoneRecord;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.SafeZoneManager;
import service.exception.MethodNotAllowedException;

public class CreateTradeRecordRestController implements Controller {
    private static final Logger log =
            LoggerFactory.getLogger(CreateTradeRecordRestController.class);

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("POST")) {
            String tradeId = (String) request.getAttribute("tradeId");
            String userId = UserSessionUtils.getCurrentUserId(request);

            SafeZoneManager safeZoneMan = SafeZoneManager.getInstance();
            safeZoneMan.findTradeRecordInfo(JsonUtils.parseId(tradeId), userId);

            List<String> photoUrls = new ArrayList<String>();
            for (Part part : request.getParts()) {
                if (part.getName().equals("photos")) {
                    photoUrls.add(FileUploadUtils.saveImage(part, "safe-zone"));
                }
            }

            SafeZoneRecord record = new SafeZoneRecord();
            record.setTradeId(JsonUtils.parseId(tradeId));
            record.setRecorderId(userId);
            record.setSafeZoneId(JsonUtils.parseId(request.getParameter("safeZoneId")));
            record.setRecordAction(request.getParameter("action"));
            record.setPhotoUrls(photoUrls);

            SafeZoneRecord saved = safeZoneMan.createRecord(record);
            log.debug("Create SafeZoneRecord : {}", saved.getRecordId());

            request.setAttribute("result", TradeRecordResponse.toRecordMap(request, saved));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
