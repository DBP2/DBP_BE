package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestMappingTest {

    @Test
    void matchExactUri() {
        Map<String, String> variables =
                RequestMapping.getPathVariables("/rest/chats", "/rest/chats");

        assertTrue(variables.isEmpty());
    }

    @Test
    void matchPathVariable() {
        Map<String, String> variables =
                RequestMapping.getPathVariables(
                        "/rest/trades/{tradeId}/records/me", "/rest/trades/7/records/me");

        assertEquals("7", variables.get("tradeId"));
    }

    @Test
    void matchIgnoresTrailingSlash() {
        assertTrue(RequestMapping.getPathVariables("/rest/chats", "/rest/chats/").isEmpty());
    }

    @Test
    void notMatchDifferentUri() {
        assertNull(
                RequestMapping.getPathVariables("/rest/chats/{roomId}", "/rest/chats/1/messages"));
        assertNull(RequestMapping.getPathVariables("/rest/chats/{roomId}", "/rest/safe-zones/1"));
    }

    @Test
    void findMyInfoAndPasswordController() {
        RequestMapping rm = new RequestMapping();
        rm.initMapping();

        assertTrue(
                rm.findRestController("/rest/users/me").getValue()
                        instanceof controller.user.rest.UserProfileRestController);
        assertTrue(
                rm.findRestController("/rest/users/me/password").getValue()
                        instanceof controller.user.rest.ChangePasswordRestController);
    }

    @Test
    void findRestControllerReturnsNullWhenNotMapped() {
        RequestMapping rm = new RequestMapping();
        rm.initMapping();

        assertNull(rm.findRestController("/rest/unknown"));
    }
}
