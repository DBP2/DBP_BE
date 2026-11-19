package controller;

import controller.chat.rest.ChatMessageRestController;
import controller.chat.rest.ChatRoomRestController;
import controller.chat.rest.ReadChatRoomRestController;
import controller.chat.rest.SendChatImageRestController;
import controller.chat.rest.ViewChatRoomRestController;
import controller.safezone.rest.ListSafeZoneRestController;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestMapping {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private Map<String, Controller> mappings = new HashMap<String, Controller>();

    public void initMapping() {
        mappings.put("/rest/safe-zones", new ListSafeZoneRestController());
        mappings.put("/rest/chats", new ChatRoomRestController());
        mappings.put("/rest/chats/{roomId}", new ViewChatRoomRestController());
        mappings.put("/rest/chats/{roomId}/messages", new ChatMessageRestController());
        mappings.put("/rest/chats/{roomId}/images", new SendChatImageRestController());
        mappings.put("/rest/chats/{roomId}/read", new ReadChatRoomRestController());

        logger.info("Initialized Request Mapping!");
    }

    public Controller findController(String uri) {
        return mappings.get(uri);
    }

    public Map.Entry<String, Controller> findRestController(String uri) {
        for (Map.Entry<String, Controller> entry : mappings.entrySet()) {
            if (getPathVariables(entry.getKey(), uri) != null) {
                return entry;
            }
        }
        return null;
    }

    public static Map<String, String> getPathVariables(String mappedUri, String uri) {
        String[] mappedParts = mappedUri.replaceAll("^/|/$", "").split("/");
        String[] uriParts = uri.replaceAll("^/|/$", "").split("/");
        if (mappedParts.length != uriParts.length) {
            return null;
        }

        Map<String, String> variables = new HashMap<String, String>();
        for (int i = 0; i < mappedParts.length; i++) {
            if (mappedParts[i].startsWith("{") && mappedParts[i].endsWith("}")) {
                if (uriParts[i].isEmpty()) {
                    return null;
                }
                String name = mappedParts[i].substring(1, mappedParts[i].length() - 1);
                variables.put(name, uriParts[i]);
            } else if (!mappedParts[i].equals(uriParts[i])) {
                return null;
            }
        }
        return variables;
    }
}
