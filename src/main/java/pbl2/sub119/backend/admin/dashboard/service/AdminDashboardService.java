package pbl2.sub119.backend.admin.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.admin.dashboard.dto.AdminDashboardResponse;
import pbl2.sub119.backend.admin.dashboard.mapper.AdminDashboardMapper;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    public AdminDashboardResponse getDashboard() {

        long operatingProductCount = adminDashboardMapper.countOperatingProducts();
        long activeMemberCount = adminDashboardMapper.countActiveMembers();
        long recruitingPartyCount = adminDashboardMapper.countRecruitingParties();
        long failedPaymentCount = adminDashboardMapper.countFailedPayments();

        long failedPaymentPartyCount = adminDashboardMapper.countFailedPaymentParties();
        long waitingMatchUserCount = adminDashboardMapper.countWaitingMatchUsers();

        return new AdminDashboardResponse(
                operatingProductCount,
                activeMemberCount,
                recruitingPartyCount,
                failedPaymentCount,
                failedPaymentPartyCount,
                waitingMatchUserCount,
                recruitingPartyCount
        );
    }
}