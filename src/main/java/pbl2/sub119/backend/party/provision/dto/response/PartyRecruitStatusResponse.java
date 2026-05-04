package pbl2.sub119.backend.party.provision.dto.response;

import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;

public record PartyRecruitStatusResponse(
        Long partyId,
        Integer currentMemberCount,
        Integer capacity,
        RecruitStatus recruitStatus,
        OperationStatus operationStatus,
        Boolean recruitCompleted,
        Boolean provisionAvailable
) {
}