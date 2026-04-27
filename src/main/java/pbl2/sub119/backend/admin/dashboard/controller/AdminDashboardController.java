package pbl2.sub119.backend.admin.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.admin.controller.docs.AdminDocs;
import pbl2.sub119.backend.admin.dashboard.dto.AdminDashboardResponse;
import pbl2.sub119.backend.admin.dashboard.service.AdminDashboardService;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
@AdminOnly
public class AdminDashboardController implements AdminDocs.Dashboard {

    private final AdminDashboardService adminDashboardService;

    @Override
    public ResponseEntity<AdminDashboardResponse> getDashboard(@Auth final Accessor accessor) {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }
}