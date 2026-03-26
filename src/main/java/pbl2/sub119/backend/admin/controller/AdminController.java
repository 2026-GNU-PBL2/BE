package pbl2.sub119.backend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.admin.controller.docs.AdminDocs;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@AdminOnly
public class AdminController implements AdminDocs {

    @Override
    public ResponseEntity<String> checkAdmin(@Auth final Accessor accessor) {
        return ResponseEntity.ok("관리자 접근 성공");
    }
}