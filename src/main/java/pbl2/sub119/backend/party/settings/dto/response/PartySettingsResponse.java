package pbl2.sub119.backend.party.settings.dto.response;

import java.time.LocalDate;

public record PartySettingsResponse(
        String role,
        String ottServiceName,
        LocalDate partyCreatedAt,
        // HOST
        Integer settlementDayOfMonth,
        Long monthlySettlementAmount,
        String settlementBankName,
        String settlementAccountMasked,
        // MEMBER
        Long monthlyPaymentAmount,
        Integer billingDayOfMonth
) {
}
