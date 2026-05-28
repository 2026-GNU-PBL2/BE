package pbl2.sub119.backend.toss.client;

import pbl2.sub119.backend.toss.dto.request.TossBillingAuthRequest;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;
import pbl2.sub119.backend.toss.dto.response.TossBillingAuthResponse;
import pbl2.sub119.backend.toss.dto.response.TossBillingPaymentResponse;

public interface PaymentGatewayClient {
    TossBillingAuthResponse issueBillingKey(TossBillingAuthRequest request);

    TossBillingPaymentResponse executeBillingPayment(
            String billingKey,
            TossBillingPaymentRequest request,
            String idempotencyKey
    );
}