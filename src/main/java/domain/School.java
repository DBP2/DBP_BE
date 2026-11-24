package domain;

/** 학교 (SCHOOL). 더미 데이터 조회 전용 */
public class School {
    private Long schoolId;
    private String schoolName;
    private String emailDomain;

    public School() {}

    public School(Long schoolId, String schoolName, String emailDomain) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.emailDomain = emailDomain;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }

    @Override
    public String toString() {
        return "School[" + schoolId + ", " + schoolName + ", " + emailDomain + "]";
    }
}
