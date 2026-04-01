package pbl2.sub119.backend.toss.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossBillingChargeResponse(
        String paymentKey,
        String orderId,
        String status,
        Long totalAmount
) {}
