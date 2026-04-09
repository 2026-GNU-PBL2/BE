package pbl2.sub119.backend.party.provision.dto.response;

import java.time.LocalDateTime;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionMemberStatus;

// 이용 완료 확인 응답
public record PartyProvisionConfirmResponse(
        Long partyId,
        Long userId,
        ProvisionMemberStatus memberStatus,
        LocalDateTime confirmedAt,
        LocalDateTime activatedAt
) {
}