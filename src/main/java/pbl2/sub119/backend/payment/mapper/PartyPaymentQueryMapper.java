package pbl2.sub119.backend.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.common.enumerated.PartyMemberRole;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.payment.dto.PartyPaymentReadinessInfo;

import java.util.Optional;

@Mapper
public interface PartyPaymentQueryMapper {

    Optional<PartyPaymentReadinessInfo> findPaymentReadinessInfo(
            @Param("partyId") Long partyId,
            @Param("memberRole") PartyMemberRole memberRole,
            @Param("pendingMemberStatus") PartyMemberStatus pendingMemberStatus,
            @Param("billingKeyStatus") BillingKeyStatus billingKeyStatus
    );
}