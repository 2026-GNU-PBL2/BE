package pbl2.sub119.backend.admin.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.admin.controller.docs.AdminDocs;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.payment.service.PaymentRetryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/payments/cycles")
@AdminOnly
public class AdminPaymentController implements AdminDocs.Payment {

    private final PaymentRetryService paymentRetryService;

    @Override
    public ResponseEntity<Void> retryPaymentCycle(
            @Auth final Accessor accessor,
            @PathVariable final Long partyCycleId
    ) {
        paymentRetryService.retry(partyCycleId, accessor.getUserId());
        return ResponseEntity.noContent().build();
    }
}
