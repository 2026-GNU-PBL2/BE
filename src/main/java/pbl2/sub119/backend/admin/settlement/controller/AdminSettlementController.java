package pbl2.sub119.backend.admin.settlement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.admin.controller.docs.AdminDocs;
import pbl2.sub119.backend.admin.settlement.dto.AdminWithdrawCompleteRequest;
import pbl2.sub119.backend.admin.settlement.dto.AdminWithdrawRejectRequest;
import pbl2.sub119.backend.admin.settlement.service.AdminSettlementWithdrawService;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.enumerated.WithdrawRequestStatus;
import pbl2.sub119.backend.settlement.dto.response.WithdrawRequestResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/settlements/withdraw-requests")
@AdminOnly
public class AdminSettlementController implements AdminDocs.Settlement {

    private final AdminSettlementWithdrawService adminSettlementWithdrawService;

    @Override
    @GetMapping
    public ResponseEntity<List<WithdrawRequestResponse>> getWithdrawRequests(
            @Auth final Accessor accessor,
            @RequestParam(defaultValue = "REQUESTED") final WithdrawRequestStatus status,
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(
                adminSettlementWithdrawService.getWithdrawRequests(status, page, size)
        );
    }

    @Override
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeWithdrawRequest(
            @Auth final Accessor accessor,
            @PathVariable final Long id,
            @Valid @RequestBody final AdminWithdrawCompleteRequest request
    ) {
        adminSettlementWithdrawService.completeWithdrawRequest(id, accessor.getUserId(), request.externalTxId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectWithdrawRequest(
            @Auth final Accessor accessor,
            @PathVariable final Long id,
            @Valid @RequestBody final AdminWithdrawRejectRequest request
    ) {
        adminSettlementWithdrawService.rejectWithdrawRequest(id, accessor.getUserId(), request.reason());
        return ResponseEntity.noContent().build();
    }
}
