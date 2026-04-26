package pbl2.sub119.backend.admin.dashboard.dto;

public record AdminDashboardResponse(
        long operatingProductCount,
        long activeMemberCount,
        long recruitingPartyCount,
        long failedPaymentCount,

        // 주의 필요 항목
        long failedPaymentPartyCount,
        long waitingMatchUserCount,
        long recruitingPartyNoticeCount
) {
}