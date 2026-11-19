package controller.chat.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Controller;
import controller.JsonUtils;
import controller.UserSessionUtils;
import domain.ChatRoom;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ChatManager;
import service.dto.ChatRoomSummary;
import service.exception.MethodNotAllowedException;

public class ChatRoomRestController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(ChatRoomRestController.class);

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String userId = UserSessionUtils.getCurrentUserId(request);

        if (request.getMethod().equals("GET")) {
            List<ChatRoomSummary> roomList = ChatManager.getInstance().findChatRoomList(userId);

            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (ChatRoomSummary summary : roomList) {
                result.add(ChatResponse.toRoomMap(request, summary));
            }

            request.setAttribute("result", result);
            return null;
        }

        if (request.getMethod().equals("POST")) {
            String jsonData = request.getReader().readLine();
            log.debug("input data: {}", jsonData);

            ObjectMapper objMapper = JsonUtils.getMapper();
            ChatRoom room = objMapper.readValue(jsonData, ChatRoom.class);

            Long chatRoomId = ChatManager.getInstance().openChatRoom(room.getPostId(), userId);
            log.debug("Open ChatRoom : {}", chatRoomId);

            request.setAttribute("result", Map.of("id", JsonUtils.toId(chatRoomId)));
            return null;
        }

        throw new MethodNotAllowedException("지원하지 않는 요청이에요");
    }
}
