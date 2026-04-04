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

        eventPublisher.publishEvent(new BillingKeyIssuedEvent(userId, partyId, response.billingKey()));
    }
}