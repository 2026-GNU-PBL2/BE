package pbl2.sub119.backend.party.settings.dto.response;

import java.time.LocalDate;

public record PartyFeeDetailResponse(
        String role,
        // HOST
        Integer memberCount,
        Long membersShareAmount,
        Long platformFee,
        Long monthlySettlementAmount,
        LocalDate nextSettlementDate,
        Boolean isSettlementGuaranteeApplied,
        // MEMBER
        Long monthlyPaymentAmount,
        Long ottUsageFee,
        LocalDate nextBillingDate
) {
}
