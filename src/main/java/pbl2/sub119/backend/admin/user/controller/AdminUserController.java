package pbl2.sub119.backend.admin.user.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.admin.controller.docs.AdminDocs;
import pbl2.sub119.backend.admin.user.dto.AdminUserDetailResponse;
import pbl2.sub119.backend.admin.user.dto.AdminUserResponse;
import pbl2.sub119.backend.admin.user.service.AdminUserService;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@AdminOnly
public class AdminUserController implements AdminDocs.User {

    private final AdminUserService adminUserService;

    @Override
    public ResponseEntity<List<AdminUserResponse>> getUsers(@Auth final Accessor accessor) {
        return ResponseEntity.ok(adminUserService.getUsers());
    }

    @Override
    public ResponseEntity<AdminUserDetailResponse> getUser(
            @Auth final Accessor accessor,
            @PathVariable final Long userId
    ) {
        return ResponseEntity.ok(adminUserService.getUser(userId));
    }
}