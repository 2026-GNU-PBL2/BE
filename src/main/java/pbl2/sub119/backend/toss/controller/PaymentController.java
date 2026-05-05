package pbl2.sub119.backend.toss.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.payment.dto.response.PaymentHistoryItem;
import pbl2.sub119.backend.payment.service.PaymentHistoryQueryService;
import pbl2.sub119.backend.toss.controller.docs.PaymentDocs;
import pbl2.sub119.backend.toss.dto.request.BillingKeyIssueRequest;
import pbl2.sub119.backend.toss.dto.response.BillingKeyInfoResponse;
import pbl2.sub119.backend.toss.service.BillingKeyService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentDocs {

    private final BillingKeyService billingKeyService;
    private final PaymentHistoryQueryService paymentHistoryQueryService;

    @PostMapping("/billing/authorize")
    public ResponseEntity<Void> issueBillingKey(
            @Auth final Accessor accessor,
            @Valid @RequestBody BillingKeyIssueRequest request) {
        billingKeyService.issueBillingKey(accessor, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/billing/customer-key")
    public ResponseEntity<Map<String, String>> getCustomerKey(
            @Auth final Accessor accessor) {
        String customerKey = "submate-" + accessor.getUserId();
        return ResponseEntity.ok(Map.of("customerKey", customerKey));
    }

    @GetMapping("/billing/me")
    public ResponseEntity<BillingKeyInfoResponse> getBillingInfo(
            @Auth final Accessor accessor) {
        return ResponseEntity.ok(billingKeyService.getBillingInfo(accessor));
    }

    @PostMapping("/billing/change")
    public ResponseEntity<Void> changeBillingKey(
            @Auth final Accessor accessor,
            @Valid @RequestBody BillingKeyIssueRequest request) {
        billingKeyService.changeBillingKey(accessor, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<PaymentHistoryItem>> getMyPaymentHistory(
            @Auth final Accessor accessor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                paymentHistoryQueryService.getMyPaymentHistory(accessor.getUserId(), page, size)
        );
    }
}
