package domain;

/** 학적 (STUDENT). 더미 데이터 조회 전용. 회원가입 요청의 학교·학번·이름과 대조 */
public class Student {
    public static final String ENROLLED = "ENROLLED";
    public static final String LEAVE = "LEAVE";
    public static final String GRADUATED = "GRADUATED";

    private Long studentId;
    private String studentNo;
    private String studentName;
    private String department;
    private String email;
    private String enrollStatus;
    private School school;

    /** 재학생만 가입할 수 있다 */
    public boolean isEnrolled() {
        return ENROLLED.equals(enrollStatus);
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEnrollStatus() {
        return enrollStatus;
    }

    public void setEnrollStatus(String enrollStatus) {
        this.enrollStatus = enrollStatus;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    @Override
    public String toString() {
        return "Student["
                + studentId
                + ", "
                + studentNo
                + ", "
                + studentName
                + ", "
                + department
                + ", "
                + enrollStatus
                + ", "
                + (school == null ? null : school.getSchoolName())
                + "]";
    }
}
