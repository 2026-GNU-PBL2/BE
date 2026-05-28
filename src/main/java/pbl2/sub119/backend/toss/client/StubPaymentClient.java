package pbl2.sub119.backend.toss.client;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.toss.dto.request.TossBillingAuthRequest;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;
import pbl2.sub119.backend.toss.dto.response.TossBillingAuthResponse;
import pbl2.sub119.backend.toss.dto.response.TossBillingPaymentResponse;
import pbl2.sub119.backend.toss.exception.PaymentException;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "payment", name = "provider", havingValue = "stub")
public class StubPaymentClient implements PaymentGatewayClient {

    private final StubPaymentProperties props;

    @Override
    public TossBillingAuthResponse issueBillingKey(TossBillingAuthRequest request) {
        sleep();
        maybeFail(ErrorCode.PAYMENT_BILLING_KEY_ISSUE_FAILED);
        return new TossBillingAuthResponse(
                "stub-bk-" + UUID.randomUUID(),
                request.customerKey(),
                "STUB_CARD",
                "1234-****-****-5678"
        );
    }

    @Override
    public TossBillingPaymentResponse executeBillingPayment(
            String billingKey,
            TossBillingPaymentRequest request,
            String idempotencyKey
    ) {
        sleep();
        maybeFail(ErrorCode.PAYMENT_BILLING_EXECUTION_FAILED);
        return new TossBillingPaymentResponse(
                "stub-pay-" + UUID.randomUUID(),
                request.orderId(),
                "DONE"
        );
    }

    private void sleep() {
        try {
            Thread.sleep(Math.max(0, props.getDelayMs()));
        } catch (InterruptedException ignored) {
        }
    }

    private void maybeFail(ErrorCode errorCode) {
        if (ThreadLocalRandom.current().nextDouble() < props.getFailRate()) {
            throw new PaymentException(errorCode);
        }
    }
}