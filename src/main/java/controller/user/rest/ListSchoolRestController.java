package controller.user.rest;

import controller.Controller;
import domain.School;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import service.UserManager;
import service.exception.MethodNotAllowedException;

/** GET /rest/schools : 가입 화면의 학교 목록 */
public class ListSchoolRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("GET")) {
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (School school : UserManager.getInstance().findSchoolList()) {
                result.add(UserResponse.toSchoolMap(school));
            }
            request.setAttribute("result", result);
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
