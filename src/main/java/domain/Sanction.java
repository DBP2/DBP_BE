package domain;

import java.time.LocalDateTime;

/** 제재 1건 (SANCTION) */
public class Sanction {
    public static final String WARNING = "WARNING";
    public static final String SUSPENSION = "SUSPENSION";
    public static final String BAN = "BAN";

    private Long sanctionId;
    private String targetId;
    private String targetNickname;
    private String adminId;
    private Long reportId;
    private String sanctionType;
    private String reason;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean released;
    private LocalDateTime releasedAt;

    /** 해제되지 않았고 기간이 남았는지 (경고 포함) */
    public boolean isActive() {
        return !released && (endAt == null || endAt.isAfter(LocalDateTime.now()));
    }

    /** 로그인·글쓰기 등을 막는 제재인지 (기간 정지·영구 정지) */
    public boolean isRestricting() {
        return isActive() && (SUSPENSION.equals(sanctionType) || BAN.equals(sanctionType));
    }

    /** 정지 종료까지 남은 일수 (종료일이 없으면 -1) */
    public long getRemainingDays() {
        if (endAt == null) {
            return -1;
        }
        long hours = java.time.Duration.between(LocalDateTime.now(), endAt).toHours();
        return hours <= 0 ? 0 : (hours + 23) / 24;
    }

    public Long getSanctionId() {
        return sanctionId;
    }

    public void setSanctionId(Long sanctionId) {
        this.sanctionId = sanctionId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetNickname() {
        return targetNickname;
    }

    public void setTargetNickname(String targetNickname) {
        this.targetNickname = targetNickname;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getSanctionType() {
        return sanctionType;
    }

    public void setSanctionType(String sanctionType) {
        this.sanctionType = sanctionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    @Override
    public String toString() {
        return "Sanction["
                + sanctionId
                + ", "
                + targetId
                + ", "
                + sanctionType
                + ", "
                + reason
                + ", ~"
                + endAt
                + ", released="
                + released
                + "]";
    }
}
