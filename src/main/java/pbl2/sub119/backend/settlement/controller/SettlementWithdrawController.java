package pbl2.sub119.backend.settlement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.settlement.controller.docs.SettlementDocs;
import pbl2.sub119.backend.settlement.dto.request.WithdrawCreateRequest;
import pbl2.sub119.backend.settlement.dto.response.PointBalanceResponse;
import pbl2.sub119.backend.settlement.dto.response.SettlementHistoryResponse;
import pbl2.sub119.backend.settlement.dto.response.WithdrawRequestResponse;
import pbl2.sub119.backend.settlement.service.SettlementWithdrawService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settlements")
public class SettlementWithdrawController implements SettlementDocs {

    private final SettlementWithdrawService settlementWithdrawService;

    @Override
    @PostMapping("/withdraw-requests")
    public ResponseEntity<WithdrawRequestResponse> createWithdrawRequest(
            @Auth final Accessor accessor,
            @Valid @RequestBody final WithdrawCreateRequest request
    ) {
        WithdrawRequestResponse response = settlementWithdrawService.createWithdrawRequest(
                accessor.getUserId(), request.amount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/withdraw-requests")
    public ResponseEntity<List<WithdrawRequestResponse>> getMyWithdrawRequests(
            @Auth final Accessor accessor,
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(
                settlementWithdrawService.getMyWithdrawRequests(accessor.getUserId(), page, size)
        );
    }

    @Override
    @GetMapping("/points")
    public ResponseEntity<PointBalanceResponse> getMyPointBalance(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(
                settlementWithdrawService.getMyPointBalance(accessor.getUserId())
        );
    }

    @Override
    @GetMapping("/history")
    public ResponseEntity<List<SettlementHistoryResponse>> getMySettlementHistory(
            @Auth final Accessor accessor,
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(
                settlementWithdrawService.getMySettlementHistory(accessor.getUserId(), page, size)
        );
    }
}
