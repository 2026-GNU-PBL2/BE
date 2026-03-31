package pbl2.sub119.backend.toss.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.toss.constant.TossPaymentClient;
import pbl2.sub119.backend.toss.dto.request.BillingKeyIssueRequest;
import pbl2.sub119.backend.toss.dto.request.TossBillingAuthRequest;
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
        Long partyId = Long.parseLong(request.partyId());

        // 중복금지
        billingKeyMapper.findByUserId(userId).ifPresent(existing -> {
            throw new PaymentException(ErrorCode.PAYMENT_BILLING_KEY_ALREADY_EXISTS);
        });



        TossBillingAuthResponse response = tossPaymentClient.issueBillingKey(
                new TossBillingAuthRequest(request.authKey(), request.customerKey())
        );

        BillingKeyEntity billingKey = BillingKeyEntity.builder()
                .userId(userId)
                .billingKey(response.billingKey())
                .customerKey(request.customerKey())
                .provider("TOSS")
                .status("ACTIVE")
                .cardCompany(response.cardCompany())
                .maskedCardNumber(response.cardNumber())
                .build();

        billingKeyMapper.insert(billingKey);

        log.info("빌링키 발급 완료. userId={}, partyId={}", userId, partyId);


        eventPublisher.publishEvent(new BillingKeyIssuedEvent(userId, partyId, response.billingKey()));
    }
}