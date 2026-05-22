package pbl2.sub119.backend.concurrent.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.controller.docs.ViolationDocs;
import pbl2.sub119.backend.concurrent.dto.response.ViolationHistoryResponse;
import pbl2.sub119.backend.concurrent.service.ViolationQueryService;

@RestController
@RequestMapping("/api/v1/violations")
@RequiredArgsConstructor
public class ViolationController implements ViolationDocs {

    private final ViolationQueryService violationQueryService;

    // 내 위반 이력 조회
    @GetMapping("/me")
    public ResponseEntity<List<ViolationHistoryResponse>> getMyViolations(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(violationQueryService.getByUserId(accessor.getUserId()));
    }
}
