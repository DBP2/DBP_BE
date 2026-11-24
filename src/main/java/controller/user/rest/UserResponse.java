package controller.user.rest;

import domain.School;
import domain.User;
import java.util.LinkedHashMap;
import java.util.Map;

/** 회원 정보를 JSON 응답용 Map 으로 바꾼다. 비밀번호·실명·학번은 응답에 넣지 않는다 */
public class UserResponse {

    /** 로그인한 본인 정보 (GET /rest/users/me, 로그인 응답). 프론트 명세: id, name(=닉네임) */
    public static Map<String, Object> toMeMap(User user) {
        Map<String, Object> map = toProfileMap(user);
        map.put("trustScore", user.getTrustScore());
        map.put("admin", user.isAdmin());
        map.put("status", user.getMemberStatus());
        return map;
    }

    /** 다른 회원에게 보이는 공개 프로필 (GET /rest/users/{userId}) */
    public static Map<String, Object> toProfileMap(User user) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", user.getUserId());
        map.put("name", user.getNickname());
        map.put("profileImage", user.getProfileImage());
        map.put("introduction", user.getIntroduction());
        map.put("trustGrade", user.getTrustGrade());
        map.put("schoolVerified", user.isSchoolVerified());
        map.put(
                "schoolName",
                user.isSchoolVerified() && user.getStudent().getSchool() != null
                        ? user.getStudent().getSchool().getSchoolName()
                        : null);
        return map;
    }

    public static Map<String, Object> toSchoolMap(School school) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", String.valueOf(school.getSchoolId()));
        map.put("name", school.getSchoolName());
        return map;
    }

    /** JSON 숫자·문자열 어느 쪽으로 와도 Long 으로 바꾼다 (형식이 틀리면 null) */
    public static Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.valueOf(((String) value).trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    public static String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
