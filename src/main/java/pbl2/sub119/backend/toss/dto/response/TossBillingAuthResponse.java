package pbl2.sub119.backend.toss.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossBillingAuthResponse(
        String billingKey,
        String customerKey,
        String cardCompany,
        String cardNumber
) {}
