package controller.chat.rest;

import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ChatManager;
import service.exception.MethodNotAllowedException;

public class ReadChatRoomRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("POST")) {
            String roomId = (String) request.getAttribute("roomId");
            Long chatRoomId = JsonUtils.parseId(roomId);
            String userId = UserSessionUtils.getCurrentUserId(request);

            ChatManager.getInstance().readMessages(chatRoomId, userId);
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
