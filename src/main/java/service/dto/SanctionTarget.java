package service.dto;

import domain.Sanction;

/**
 * 관리자 제재 대상 목록의 한 줄. 회원 + 받은 신고 수 + 미처리 신고 수 + 현재 제재를 한 번에 담는 조회 결과라 도메인으로 표현할 수 없어 DTO 로 정의 (DB 에
 * 저장하지 않음).
 */
public class SanctionTarget {
    private String userId;
    private String nickname;
    private int trustScore;
    private String memberStatus;
    private int reportCount;
    private int pendingCount;
    private Sanction activeSanction;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Sanction getActiveSanction() {
        return activeSanction;
    }

    public void setActiveSanction(Sanction activeSanction) {
        this.activeSanction = activeSanction;
    }

    @Override
    public String toString() {
        return "SanctionTarget["
                + userId
                + ", reports "
                + reportCount
                + ", pending "
                + pendingCount
                + ", "
                + activeSanction
                + "]";
    }
}
