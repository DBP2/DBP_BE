package controller.safezone.rest;

import controller.JsonUtils;
import domain.SafeZone;
import java.util.LinkedHashMap;
import java.util.Map;

public class SafeZoneResponse {

    public static Map<String, Object> toMap(SafeZone safeZone) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", JsonUtils.toId(safeZone.getSafeZoneId()));
        map.put("name", safeZone.getZoneName());
        map.put("location", safeZone.getLocationDetail());
        map.put("operatingHours", safeZone.getOperatingHours());
        return map;
    }
}
