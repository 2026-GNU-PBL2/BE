package pbl2.sub119.backend.toss.dto.request;

public record TossBillingChargeRequest(
        String customerKey,
        Long amount,
        String orderId,
        String orderName
) {}
