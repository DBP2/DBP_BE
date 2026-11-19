package controller.chat.rest;

import controller.Controller;
import controller.FileUploadUtils;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.ChatMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ChatManager;
import service.exception.MethodNotAllowedException;

public class SendChatImageRestController implements Controller {
    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getMethod().equals("POST")) {
            String roomId = (String) request.getAttribute("roomId");
            Long chatRoomId = JsonUtils.parseId(roomId);
            String userId = UserSessionUtils.getCurrentUserId(request);

            ChatManager chatMan = ChatManager.getInstance();
            chatMan.checkParticipant(chatRoomId, userId);

            String imageUrl = FileUploadUtils.saveImage(request.getPart("image"), "chat");
            ChatMessage sent = chatMan.sendImage(chatRoomId, userId, imageUrl);

            request.setAttribute("result", ChatResponse.toMessageMap(request, sent));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
