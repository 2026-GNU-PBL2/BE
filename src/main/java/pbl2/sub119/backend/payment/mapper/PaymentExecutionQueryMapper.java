package pbl2.sub119.backend.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.payment.dto.PaymentChargeTarget;

import java.util.List;

@Mapper
public interface PaymentExecutionQueryMapper {

    int countMembersByStatus(
            @Param("partyId") Long partyId,
            @Param("memberRole") PartyRole memberRole,
            @Param("memberStatus") PartyMemberStatus memberStatus
    );

    int countRecurringBillableMembers(
            @Param("partyId") Long partyId,
            @Param("memberRole") PartyRole memberRole
    );

    List<PaymentChargeTarget> findChargeTargets(
            @Param("partyId") Long partyId,
            @Param("partyCycleId") Long partyCycleId,
            @Param("memberRole") PartyRole memberRole,
            @Param("memberStatus") PartyMemberStatus memberStatus,
            @Param("billingKeyStatus") BillingKeyStatus billingKeyStatus
    );

    List<PaymentChargeTarget> findRecurringChargeTargets(
            @Param("partyId") Long partyId,
            @Param("partyCycleId") Long partyCycleId,
            @Param("memberRole") PartyRole memberRole,
            @Param("billingKeyStatus") BillingKeyStatus billingKeyStatus
    );
}
