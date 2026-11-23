package controller.admin.rest;

import controller.JsonUtils;
import domain.Sanction;
import java.util.LinkedHashMap;
import java.util.Map;
import service.dto.SanctionTarget;

/** 제재 정보를 JSON 응답용 Map 으로 바꾼다 */
public class SanctionResponse {

    public static Map<String, Object> toSanctionMap(Sanction sanction) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", JsonUtils.toId(sanction.getSanctionId()));
        map.put("targetId", sanction.getTargetId());
        map.put("targetName", sanction.getTargetNickname());
        map.put("adminId", sanction.getAdminId());
        map.put("reportId", JsonUtils.toId(sanction.getReportId()));
        map.put("type", sanction.getSanctionType());
        map.put("reason", sanction.getReason());
        map.put("startAt", JsonUtils.formatDateTime(sanction.getStartAt()));
        map.put("endAt", JsonUtils.formatDateTime(sanction.getEndAt()));
        map.put("released", sanction.isReleased());
        map.put("releasedAt", JsonUtils.formatDateTime(sanction.getReleasedAt()));
        map.put("active", sanction.isActive());
        return map;
    }

    /** 관리자 제재 대상 목록의 한 줄 */
    public static Map<String, Object> toTargetMap(SanctionTarget target) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", target.getUserId());
        map.put("name", target.getNickname());
        map.put("trustScore", target.getTrustScore());
        map.put("status", target.getMemberStatus());
        map.put("reportCount", target.getReportCount());
        map.put("pendingReportCount", target.getPendingCount());
        map.put(
                "activeSanction",
                target.getActiveSanction() == null
                        ? null
                        : toSanctionMap(target.getActiveSanction()));
        return map;
    }
}
