package pbl2.sub119.backend.toss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.toss.client.TossPaymentClient;
import pbl2.sub119.backend.toss.dto.request.BillingKeyIssueRequest;
import pbl2.sub119.backend.toss.dto.request.TossBillingAuthRequest;
import pbl2.sub119.backend.toss.dto.response.BillingKeyInfoResponse;
import pbl2.sub119.backend.toss.dto.response.TossBillingAuthResponse;
import pbl2.sub119.backend.toss.entity.BillingKeyEntity;
import pbl2.sub119.backend.toss.event.BillingKeyIssuedEvent;
import pbl2.sub119.backend.toss.exception.PaymentException;
import pbl2.sub119.backend.toss.mapper.BillingKeyMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingKeyService {

    private final TossPaymentClient tossPaymentClient;
    private final BillingKeyMapper billingKeyMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void issueBillingKey(Accessor accessor, BillingKeyIssueRequest request) {
        Long userId = accessor.getUserId();

        billingKeyMapper.findByUserId(userId).ifPresent(existing -> {
            throw new PaymentException(ErrorCode.PAYMENT_BILLING_KEY_ALREADY_EXISTS);
        });

        String customerKey = "submate-" + userId;

        TossBillingAuthResponse response = tossPaymentClient.issueBillingKey(
                new TossBillingAuthRequest(request.authKey(), customerKey)
        );

        BillingKeyEntity billingKey = BillingKeyEntity.builder()
                .userId(userId)
                .billingKey(response.billingKey())
                .customerKey(customerKey)
                .provider("TOSS")
                .status(BillingKeyStatus.ACTIVE)
                .cardCompany(response.cardCompany())
                .maskedCardNumber(response.cardNumber())
                .build();

        billingKeyMapper.insert(billingKey);

        eventPublisher.publishEvent(new BillingKeyIssuedEvent(userId, response.billingKey()));
    }

    @Transactional(readOnly = true)
    public BillingKeyInfoResponse getBillingInfo(Accessor accessor) {
        return billingKeyMapper.findByUserId(accessor.getUserId())
                .map(BillingKeyInfoResponse::of)
                .orElse(BillingKeyInfoResponse.empty());
    }

    @Transactional
    public void changeBillingKey(Accessor accessor, BillingKeyIssueRequest request) {
        Long userId = accessor.getUserId();

        BillingKeyEntity existing = billingKeyMapper.findAnyByUserId(userId)
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_BILLING_KEY_NOT_FOUND));

        String customerKey = "submate-" + userId;

        log.info("빌링키 변경 요청. userId={}, customerKey={}", userId, customerKey);

        TossBillingAuthResponse response = tossPaymentClient.issueBillingKey(
                new TossBillingAuthRequest(request.authKey(), customerKey)
        );

        billingKeyMapper.updateBillingKeyInfo(
                existing.getId(),
                response.billingKey(),
                response.cardCompany(),
                response.cardNumber()
        );

        log.info("빌링키 변경 완료. userId={}", userId);
    }
}
