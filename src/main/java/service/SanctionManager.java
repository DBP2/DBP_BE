package service;

import domain.Sanction;
import domain.User;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import persistence.dao.SanctionDAO;
import persistence.dao.UserDAO;
import service.dto.SanctionTarget;
import service.exception.BadRequestException;
import service.exception.ConflictException;
import service.exception.ForbiddenException;
import service.exception.NotFoundException;

/** 관리자 제재 처리. 모든 메소드는 요청한 회원이 관리자인지 먼저 확인한다 */
public class SanctionManager {
    public static final int MAX_SUSPENSION_DAYS = 365;
    public static final int MAX_REASON_BYTES = 500;

    private static SanctionManager sanctionMan = new SanctionManager();
    private SanctionDAO sanctionDAO;
    private UserDAO userDAO;

    private SanctionManager() {
        try {
            sanctionDAO = new SanctionDAO();
            userDAO = new UserDAO();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SanctionManager getInstance() {
        return sanctionMan;
    }

    /**
     * 제재 대상 목록
     *
     * @param filter pending(미처리 신고 있음, 기본) / sanctioned(정지 중) / all
     */
    public List<SanctionTarget> findTargetList(String adminId, String filter)
            throws SQLException, ForbiddenException, BadRequestException {
        checkAdmin(adminId);
        if (filter == null || filter.isBlank()) {
            filter = "pending";
        }
        if (!List.of("pending", "sanctioned", "all").contains(filter)) {
            throw new BadRequestException("filter 는 pending, sanctioned, all 중 하나여야 해요");
        }
        List<SanctionTarget> targetList = sanctionDAO.findTargetList(filter);
        if (targetList == null) {
            throw new SQLException("제재 대상 목록을 불러오지 못했어요");
        }
        return targetList;
    }

    /** 한 회원의 제재 이력 (최근 순) */
    public List<Sanction> findSanctionList(String adminId, String userId)
            throws SQLException, ForbiddenException, NotFoundException {
        checkAdmin(adminId);
        if (userDAO.findUser(userId) == null) {
            throw new NotFoundException("회원을 찾을 수 없어요");
        }
        List<Sanction> sanctionList = sanctionDAO.findListByUser(userId);
        if (sanctionList == null) {
            throw new SQLException("제재 이력을 불러오지 못했어요");
        }
        return sanctionList;
    }

    /**
     * 제재 적용. 기간 정지(SUSPENSION)는 days 일 뒤에 끝나고, 경고(WARNING)·영구 정지(BAN)는 기간이 없다.
     *
     * @param reportId 처리한 신고 (없으면 null)
     * @return 적용된 제재
     */
    public Sanction applySanction(
            String adminId,
            String targetId,
            String sanctionType,
            String reason,
            Integer days,
            Long reportId)
            throws SQLException, ForbiddenException, BadRequestException, NotFoundException {
        checkAdmin(adminId);

        User target = userDAO.findUser(targetId);
        if (target == null) {
            throw new NotFoundException("제재할 회원을 찾을 수 없어요");
        }
        if (target.isAdmin()) {
            throw new BadRequestException("관리자는 제재할 수 없어요");
        }
        if (!List.of(Sanction.WARNING, Sanction.SUSPENSION, Sanction.BAN).contains(sanctionType)) {
            throw new BadRequestException("제재 종류는 WARNING, SUSPENSION, BAN 중 하나여야 해요");
        }
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("제재 사유를 입력해 주세요");
        }
        if (reason.trim().getBytes(StandardCharsets.UTF_8).length > MAX_REASON_BYTES) {
            throw new BadRequestException("제재 사유는 한글 160자 이내로 써 주세요");
        }

        Sanction sanction = new Sanction();
        sanction.setTargetId(targetId);
        sanction.setAdminId(adminId);
        sanction.setReportId(reportId);
        sanction.setSanctionType(sanctionType);
        sanction.setReason(reason.trim());
        if (Sanction.SUSPENSION.equals(sanctionType)) {
            if (days == null || days < 1 || days > MAX_SUSPENSION_DAYS) {
                throw new BadRequestException("정지 기간은 1~365일이어야 해요");
            }
            sanction.setEndAt(LocalDateTime.now().plusDays(days));
        }

        Long sanctionId = sanctionDAO.applySanction(sanction);
        if (sanctionId == null) {
            throw new SQLException("제재를 적용하지 못했어요");
        }
        return sanctionDAO.findSanction(sanctionId);
    }

    /** 제재 해제. 남은 정지가 없으면 회원 상태도 ACTIVE 로 돌아간다 */
    public void releaseSanction(String adminId, Long sanctionId)
            throws ForbiddenException, NotFoundException, ConflictException {
        checkAdmin(adminId);

        Sanction sanction = sanctionId == null ? null : sanctionDAO.findSanction(sanctionId);
        if (sanction == null) {
            throw new NotFoundException("제재를 찾을 수 없어요");
        }
        if (sanction.isReleased()) {
            throw new ConflictException("이미 해제된 제재예요");
        }
        sanctionDAO.release(sanctionId);
    }

    private void checkAdmin(String adminId) throws ForbiddenException {
        User admin = adminId == null ? null : userDAO.findUser(adminId);
        if (admin == null || !admin.isAdmin()) {
            throw new ForbiddenException("관리자만 할 수 있어요");
        }
    }
}
