package pbl2.sub119.backend.toss.dto.request;

public record TossBillingPaymentRequest(
        String customerKey,
        Integer amount,
        String orderId,
        String orderName
) {
}