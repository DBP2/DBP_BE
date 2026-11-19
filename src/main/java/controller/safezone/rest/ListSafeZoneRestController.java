package controller.safezone.rest;

import controller.Controller;
import domain.SafeZone;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import service.SafeZoneManager;
import service.exception.MethodNotAllowedException;

public class ListSafeZoneRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("GET")) {
            List<SafeZone> safeZoneList = SafeZoneManager.getInstance().findSafeZoneList();

            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (SafeZone safeZone : safeZoneList) {
                result.add(SafeZoneResponse.toMap(safeZone));
            }

            request.setAttribute("result", result);
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
