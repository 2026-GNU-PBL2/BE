package pbl2.sub119.backend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.admin.controller.docs.AdminDocs;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.enumerated.UserRole;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController implements AdminDocs {

    @Override
    public ResponseEntity<String> checkAdmin(@Auth final Accessor accessor) {
        if (accessor.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자만 접근할 수 있습니다.");
        }

        return ResponseEntity.ok("관리자 접근 성공");
    }
}