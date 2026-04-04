package pbl2.sub119.backend.toss.dto.response;

public record TossBillingPaymentResponse(
        String paymentKey,
        String orderId,
        String status
) {
}
