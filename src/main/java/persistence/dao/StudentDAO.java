package persistence.dao;

import domain.School;
import domain.Student;

import persistence.util.JDBCUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** SCHOOL · STUDENT (더미 학교·학적). 회원가입 학적 확인에 함께 쓰여 하나의 DAO 로 관리 */
public class StudentDAO {

    private JDBCUtil jdbcUtil = null;

    public StudentDAO() {
        jdbcUtil = new JDBCUtil();
    }

    /** 가입 화면의 학교 선택 목록 */
    public List<School> findSchoolList() {
        String sql = "SELECT SCHOOL_ID, SCHOOL_NAME, EMAIL_DOMAIN FROM SCHOOL ORDER BY SCHOOL_ID";
        jdbcUtil.setSqlAndParameters(sql, null);
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            List<School> list = new ArrayList<School>();
            while (rs.next()) {
                list.add(
                        new School(
                                rs.getLong("SCHOOL_ID"),
                                rs.getString("SCHOOL_NAME"),
                                rs.getString("EMAIL_DOMAIN")));
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    /** 학교 + 학번으로 학적 1건 조회 (없으면 null). 이름 일치·재학 여부 판단은 Manager 에서 */
    public Student findStudent(Long schoolId, String studentNo) {
        String sql =
                "SELECT s.STUDENT_ID, s.STUDENT_NO, s.STUDENT_NAME, s.DEPARTMENT, s.EMAIL,"
                    + " s.ENROLL_STATUS, sc.SCHOOL_ID, sc.SCHOOL_NAME, sc.EMAIL_DOMAIN FROM STUDENT"
                    + " s JOIN SCHOOL sc ON s.SCHOOL_ID = sc.SCHOOL_ID WHERE s.SCHOOL_ID = ? AND"
                    + " s.STUDENT_NO = ?";
        jdbcUtil.setSqlAndParameters(sql, new Object[] {schoolId, studentNo});
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                return toStudent(rs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    /** ResultSet 의 현재 행 → Student (학교 포함). UserDAO 에서도 사용 */
    static Student toStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getLong("STUDENT_ID"));
        s.setStudentNo(rs.getString("STUDENT_NO"));
        s.setStudentName(rs.getString("STUDENT_NAME"));
        s.setDepartment(rs.getString("DEPARTMENT"));
        s.setEmail(rs.getString("EMAIL"));
        s.setEnrollStatus(rs.getString("ENROLL_STATUS"));
        s.setSchool(
                new School(
                        rs.getLong("SCHOOL_ID"),
                        rs.getString("SCHOOL_NAME"),
                        rs.getString("EMAIL_DOMAIN")));
        return s;
    }
}
