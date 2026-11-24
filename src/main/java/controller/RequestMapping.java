package controller;

import controller.admin.rest.ReleaseSanctionRestController;
import controller.admin.rest.SanctionRestController;
import controller.admin.rest.SanctionTargetRestController;
import controller.admin.rest.UserSanctionRestController;
import controller.chat.rest.ChatMessageRestController;
import controller.chat.rest.ChatRoomRestController;
import controller.chat.rest.ReadChatRoomRestController;
import controller.chat.rest.SendChatImageRestController;
import controller.chat.rest.ViewChatRoomRestController;
import controller.safezone.rest.CreateTradeRecordRestController;
import controller.safezone.rest.ListSafeZoneRestController;
import controller.safezone.rest.VerifySafeZoneRestController;
import controller.safezone.rest.ViewMyTradeRecordRestController;
import controller.safezone.rest.ViewTradeRecordInfoRestController;
import controller.user.rest.ChangePasswordRestController;
import controller.user.rest.ListSchoolRestController;
import controller.user.rest.LoginRestController;
import controller.user.rest.LogoutRestController;
import controller.user.rest.UserProfileRestController;
import controller.user.rest.UserRestController;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestMapping {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private Map<String, Controller> mappings = new HashMap<String, Controller>();

    public void initMapping() {
        mappings.put("/rest/safe-zones", new ListSafeZoneRestController());
        mappings.put("/rest/trades/{tradeId}/safe-zone/verify", new VerifySafeZoneRestController());
        mappings.put("/rest/trades/{tradeId}/record-info", new ViewTradeRecordInfoRestController());
        mappings.put("/rest/trades/{tradeId}/records/me", new ViewMyTradeRecordRestController());
        mappings.put("/rest/trades/{tradeId}/records", new CreateTradeRecordRestController());
        mappings.put("/rest/chats", new ChatRoomRestController());
        mappings.put("/rest/chats/{roomId}", new ViewChatRoomRestController());
        mappings.put("/rest/chats/{roomId}/messages", new ChatMessageRestController());
        mappings.put("/rest/chats/{roomId}/images", new SendChatImageRestController());
        mappings.put("/rest/chats/{roomId}/read", new ReadChatRoomRestController());

        // 회원가입·로그인·내 프로필
        mappings.put("/rest/schools", new ListSchoolRestController());
        mappings.put("/rest/users", new UserRestController());
        mappings.put("/rest/users/{userId}", new UserProfileRestController()); // users/me 포함
        mappings.put("/rest/users/me/password", new ChangePasswordRestController());
        mappings.put("/rest/login", new LoginRestController());
        mappings.put("/rest/logout", new LogoutRestController());

        // 관리자 제재
        mappings.put("/rest/admin/sanction-targets", new SanctionTargetRestController());
        mappings.put("/rest/admin/sanctions", new SanctionRestController());
        mappings.put(
                "/rest/admin/sanctions/{sanctionId}/release", new ReleaseSanctionRestController());
        mappings.put("/rest/admin/users/{userId}/sanctions", new UserSanctionRestController());

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
