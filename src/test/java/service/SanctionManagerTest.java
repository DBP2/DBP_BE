package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.Sanction;
import domain.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import persistence.dao.MemberTestFixture;
import service.exception.BadRequestException;
import service.exception.ConflictException;
import service.exception.ForbiddenException;

@Tag("db")
class SanctionManagerTest {
    private final SanctionManager sanctionMan = SanctionManager.getInstance();

    @BeforeAll
    static void setUp() {
        MemberTestFixture.setUp();
    }

    @AfterAll
    static void tearDown() {
        MemberTestFixture.tearDown();
    }

    @BeforeEach
    void reset() {
        MemberTestFixture.resetSanctions();
    }

    @Test
    void notAdmin() {
        assertThrows(
                ForbiddenException.class,
                () -> sanctionMan.findTargetList(MemberTestFixture.USER_ID, "pending"));
    }

    @Test
    void suspensionNeedsDays() {
        assertThrows(
                BadRequestException.class,
                () ->
                        sanctionMan.applySanction(
                                MemberTestFixture.ADMIN_ID,
                                MemberTestFixture.USER_ID,
                                Sanction.SUSPENSION,
                                "노쇼",
                                null,
                                null));
    }

    @Test
    void applyAndRelease() throws Exception {
        Sanction sanction =
                sanctionMan.applySanction(
                        MemberTestFixture.ADMIN_ID,
                        MemberTestFixture.USER_ID,
                        Sanction.SUSPENSION,
                        "노쇼",
                        3,
                        MemberTestFixture.REPORT_ID);
        assertNotNull(sanction.getSanctionId());
        assertEquals(
                User.SUSPENDED,
                UserManager.getInstance().findUser(MemberTestFixture.USER_ID).getMemberStatus());

        sanctionMan.releaseSanction(MemberTestFixture.ADMIN_ID, sanction.getSanctionId());
        assertEquals(
                User.ACTIVE,
                UserManager.getInstance().findUser(MemberTestFixture.USER_ID).getMemberStatus());
        assertThrows(
                ConflictException.class,
                () ->
                        sanctionMan.releaseSanction(
                                MemberTestFixture.ADMIN_ID, sanction.getSanctionId()));
    }

    @Test
    void cannotSanctionAdmin() {
        assertThrows(
                BadRequestException.class,
                () ->
                        sanctionMan.applySanction(
                                MemberTestFixture.ADMIN_ID,
                                MemberTestFixture.ADMIN_ID,
                                Sanction.WARNING,
                                "테스트",
                                null,
                                null));
    }
}
