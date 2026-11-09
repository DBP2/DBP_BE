package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.School;
import domain.Student;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

@Tag("db")
class StudentDAOTest {

    private final StudentDAO dao = new StudentDAO();

    @BeforeAll
    static void setUp() {
        MemberTestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        MemberTestFixture.tearDown();
    }

    @Test
    void findSchoolList() {
        List<School> list = dao.findSchoolList();

        assertNotNull(list);
        assertTrue(
                list.stream().anyMatch(s -> s.getSchoolId().equals(MemberTestFixture.SCHOOL_ID)));
    }

    @Test
    void findStudent() {
        Student student =
                dao.findStudent(MemberTestFixture.SCHOOL_ID, MemberTestFixture.STUDENT_NO);

        assertNotNull(student);
        assertEquals("테스트학생", student.getStudentName());
        assertEquals("테스트대학교", student.getSchool().getSchoolName());
        assertTrue(student.isEnrolled());
    }

    @Test
    void findStudentOnLeave() {
        Student student = dao.findStudent(MemberTestFixture.SCHOOL_ID, "T900002");

        assertNotNull(student);
        assertFalse(student.isEnrolled());
    }

    @Test
    void findStudentNotFound() {
        assertNull(dao.findStudent(MemberTestFixture.SCHOOL_ID, "NO_SUCH_NO"));
    }
}
