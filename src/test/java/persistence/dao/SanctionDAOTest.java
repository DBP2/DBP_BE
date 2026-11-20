package persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.Sanction;
import domain.User;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import service.dto.SanctionTarget;

import java.time.LocalDateTime;
import java.util.List;

@Tag("db")
class SanctionDAOTest {

    private final SanctionDAO dao = new SanctionDAO();
    private final UserDAO userDAO = new UserDAO();

    @BeforeAll
    static void setUp() {
        MemberTestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        MemberTestFixture.tearDown();
    }

    @AfterEach
    void reset() {
        MemberTestFixture.resetSanctions();
    }

    private Sanction newSanction(String type, LocalDateTime endAt) {
        Sanction s = new Sanction();
        s.setTargetId(MemberTestFixture.USER_ID);
        s.setAdminId(MemberTestFixture.ADMIN_ID);
        s.setSanctionType(type);
        s.setReason("테스트 제재");
        s.setEndAt(endAt);
        return s;
    }

    private String status() {
        return userDAO.findUser(MemberTestFixture.USER_ID).getMemberStatus();
    }

    @Test
    void applySuspensionAndRelease() {
        Long id =
                dao.applySanction(
                        newSanction(Sanction.SUSPENSION, LocalDateTime.now().plusDays(3)));

        assertNotNull(id);
        assertEquals(User.SUSPENDED, status());
        Sanction active = dao.findActiveByUser(MemberTestFixture.USER_ID);
        assertEquals(id, active.getSanctionId());
        assertTrue(active.isRestricting());

        assertEquals(1, dao.release(id));
        assertEquals(User.ACTIVE, status());
        assertNull(dao.findActiveByUser(MemberTestFixture.USER_ID));
        assertEquals(0, dao.release(id)); // 이미 해제됨
    }

    @Test
    void applyWarningKeepsActive() {
        Long id = dao.applySanction(newSanction(Sanction.WARNING, null));

        assertNotNull(id);
        assertEquals(User.ACTIVE, status());
        assertEquals(Sanction.WARNING, dao.findSanction(id).getSanctionType());
    }

    @Test
    void invalidSanctionIsRolledBack() {
        // 기간 정지인데 종료일이 없으면 SANCTION_PERIOD_CK 위반 → 회원 상태도 바뀌지 않아야 함
        assertNull(dao.applySanction(newSanction(Sanction.SUSPENSION, null)));
        assertEquals(User.ACTIVE, status());
        assertTrue(dao.findListByUser(MemberTestFixture.USER_ID).isEmpty());
    }

    @Test
    void refreshStatusAfterExpiry() {
        MemberTestFixture.execute(
                "UPDATE MEMBER SET MEMBER_STATUS = 'SUSPENDED' WHERE USER_ID = ?",
                new Object[] {MemberTestFixture.USER_ID});

        assertEquals(1, dao.refreshStatus(MemberTestFixture.USER_ID)); // 유효한 정지가 없으므로 해제
        assertEquals(User.ACTIVE, status());
    }

    @Test
    void findTargetList() {
        Sanction s = newSanction(Sanction.WARNING, null);
        s.setReportId(MemberTestFixture.REPORT_ID);
        dao.applySanction(s);

        List<SanctionTarget> list = dao.findTargetList("all");

        assertNotNull(list);
        SanctionTarget target =
                list.stream()
                        .filter(t -> t.getUserId().equals(MemberTestFixture.USER_ID))
                        .findFirst()
                        .orElse(null);
        assertNotNull(target);
        assertEquals(1, target.getReportCount());
        assertEquals(0, target.getPendingCount()); // 신고가 제재와 연결됨
        assertNotNull(target.getActiveSanction());
    }
}
