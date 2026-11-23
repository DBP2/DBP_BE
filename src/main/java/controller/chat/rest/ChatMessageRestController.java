package controller.chat.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.ChatMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ChatManager;
import service.exception.MethodNotAllowedException;

public class ChatMessageRestController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(ChatMessageRestController.class);

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String roomId = (String) request.getAttribute("roomId");
        Long chatRoomId = JsonUtils.parseId(roomId);
        String userId = UserSessionUtils.getCurrentUserId(request);

        if (request.getMethod().equals("GET")) {
            Long afterMessageId = JsonUtils.parseId(request.getParameter("after"));
            List<ChatMessage> messageList =
                    ChatManager.getInstance().findMessageList(chatRoomId, userId, afterMessageId);

            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (ChatMessage message : messageList) {
                result.add(ChatResponse.toMessageMap(request, message));
            }

            request.setAttribute("result", result);
            return null;
        }

        if (request.getMethod().equals("POST")) {
            String jsonData = request.getReader().readLine();
            log.debug("input data: {}", jsonData);

            ObjectMapper objMapper = JsonUtils.getMapper();
            ChatMessage message = objMapper.readValue(jsonData, ChatMessage.class);

            ChatMessage sent =
                    ChatManager.getInstance().sendMessage(chatRoomId, userId, message.getContent());
            log.debug("Send ChatMessage : {}", sent.getMessageId());

            request.setAttribute("result", ChatResponse.toMessageMap(request, sent));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
