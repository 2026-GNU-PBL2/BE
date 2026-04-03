package pbl2.sub119.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartyPaymentReadinessInfo {

    private Long partyId;
    private Integer capacity;
    private Integer currentMemberCount;
    private Integer pendingMemberCount;
    private Integer readyBillingMemberCount;
    private Integer pricePerMemberSnapshot;

    public boolean isCapacityFilled() {
        return currentMemberCount != null && capacity != null && currentMemberCount.equals(capacity);
    }

    public boolean hasPendingMembers() {
        return pendingMemberCount != null && pendingMemberCount > 0;
    }

    public boolean isBillingReady() {
        return pendingMemberCount != null
                && readyBillingMemberCount != null
                && pendingMemberCount.equals(readyBillingMemberCount);
    }

    public boolean isReadyForInitialPayment() {
        return isCapacityFilled() && hasPendingMembers() && isBillingReady();
    }
}