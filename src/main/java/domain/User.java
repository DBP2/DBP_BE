package domain;

import java.time.LocalDateTime;

/** 회원 (MEMBER). USER 는 Oracle 예약어라 테이블 이름은 MEMBER */
public class User {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ACTIVE = "ACTIVE";
    public static final String SUSPENDED = "SUSPENDED";
    public static final String BANNED = "BANNED";
    public static final String WITHDRAWN = "WITHDRAWN";

    private String userId;
    private String password;
    private String nickname;
    private String profileImage;
    private String introduction;
    private int trustScore;
    private String memberRole = ROLE_USER;
    private String memberStatus = ACTIVE;
    private LocalDateTime createdAt;
    private Student student;

    public User() {}

    public User(String userId, String password, String nickname, Student student) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.student = student;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(memberRole);
    }

    public boolean isActive() {
        return ACTIVE.equals(memberStatus);
    }

    /** 학적이 연결되어 있으면 학교 인증 회원 */
    public boolean isSchoolVerified() {
        return student != null;
    }

    public boolean matchPassword(String pw) {
        return password != null && password.equals(pw);
    }

    /** 신뢰등급: A(30 이상) / B(10~29) / C(0~9, 가입 시) / D(0 미만) */
    public String getTrustGrade() {
        if (trustScore >= 30) {
            return "A";
        }
        if (trustScore >= 10) {
            return "B";
        }
        if (trustScore >= 0) {
            return "C";
        }
        return "D";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "User["
                + userId
                + ", "
                + nickname
                + ", "
                + memberRole
                + ", "
                + memberStatus
                + ", trust "
                + trustScore
                + "("
                + getTrustGrade()
                + "), "
                + student
                + "]";
    }
}
