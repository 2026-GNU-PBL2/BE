package pbl2.sub119.backend.toss.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.toss.controller.docs.PaymentDocs;
import pbl2.sub119.backend.toss.dto.request.BillingKeyIssueRequest;
import pbl2.sub119.backend.toss.service.BillingKeyService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentDocs {

    private final BillingKeyService billingKeyService;

    /**
     * 빌링키 발급
     * 프론트에서 토스 결제창 띄운 후 authKey 받아서 여기로 전달
     * POST /api/v1/payments/billing/authorize
     */
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

}
