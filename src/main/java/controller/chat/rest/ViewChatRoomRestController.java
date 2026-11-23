package controller.chat.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ChatManager;
import service.dto.ChatRoomDetail;
import service.exception.MethodNotAllowedException;

public class ViewChatRoomRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("GET")) {
            String roomId = (String) request.getAttribute("roomId");
            Long chatRoomId = JsonUtils.parseId(roomId);
            String userId = UserSessionUtils.getCurrentUserId(request);

            ChatRoomDetail detail = ChatManager.getInstance().findChatRoom(chatRoomId, userId);

            request.setAttribute("result", ChatResponse.toDetailMap(request, detail));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
