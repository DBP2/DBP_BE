package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.exception.ServiceException;

public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    public static final Object NULL_RESULT = new Object();

    private RequestMapping rm;

    @Override
    public void init() throws ServletException {
        rm = new RequestMapping();
        rm.initMapping();
        FileUploadUtils.setUploadRoot(getServletContext().getInitParameter("uploadDir"));
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug(
                "Method : {}, Request URI : {}, ServletPath : {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getServletPath());
        String servletPath = request.getServletPath();

        try {
            if (servletPath.startsWith("/rest/")) {
                Map.Entry<String, Controller> entry = rm.findRestController(servletPath);
                if (entry == null) {
                    writeError(response, 404, "요청한 API를 찾을 수 없어요");
                    return;
                }

                Controller controller = entry.getValue();
                String mappedUri = entry.getKey();
                Map<String, String> parameters =
                        RequestMapping.getPathVariables(mappedUri, servletPath);
                for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                    request.setAttribute(parameter.getKey(), parameter.getValue());
                }

                controller.execute(request, response);

                Object result = request.getAttribute("result");
                Integer status = (Integer) request.getAttribute("status");
                if (result != null) {
                    ObjectMapper mapper = JsonUtils.getMapper();
                    String jsonString =
                            mapper.writeValueAsString(result == NULL_RESULT ? null : result);
                    logger.debug("result in JSON: {}", jsonString);

                    response.setStatus(status != null ? status : HttpServletResponse.SC_OK);
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().println(jsonString);
                } else {
                    response.setStatus(
                            status != null ? status : HttpServletResponse.SC_NO_CONTENT);
                }
                return;
            }

            writeError(response, 404, "요청한 API를 찾을 수 없어요");
        } catch (ServiceException e) {
            logger.debug("ServiceException : {}", e.getMessage());
            writeError(response, e.getStatus(), e.getMessage());
        } catch (JsonProcessingException e) {
            logger.debug("JsonProcessingException : {}", e.getMessage());
            writeError(response, 400, "요청 형식이 올바르지 않아요");
        } catch (Exception e) {
            logger.error("Exception : {}", e);
            writeError(response, 500, "서버 오류가 발생했어요");
        }
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        ObjectMapper mapper = JsonUtils.getMapper();
        String jsonString = mapper.writeValueAsString(Map.of("message", message));

        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(jsonString);
    }
}
