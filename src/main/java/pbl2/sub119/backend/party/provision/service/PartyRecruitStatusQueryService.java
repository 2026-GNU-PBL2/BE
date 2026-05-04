package pbl2.sub119.backend.party.provision.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.provision.dto.response.PartyRecruitStatusResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyRecruitStatusQueryService {

    private final PartyMapper partyMapper;

    // 파티 모집 완료 여부 조회
    public PartyRecruitStatusResponse getRecruitStatus(final Long partyId) {
        final Party party = partyMapper.findById(partyId);

        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        final boolean recruitCompleted = party.getRecruitStatus() == RecruitStatus.FULL
                || party.getCurrentMemberCount() >= party.getCapacity();

        final boolean provisionAvailable = recruitCompleted
                && party.getOperationStatus() == OperationStatus.ACTIVE;

        return new PartyRecruitStatusResponse(
                party.getId(),
                party.getCurrentMemberCount(),
                party.getCapacity(),
                party.getRecruitStatus(),
                party.getOperationStatus(),
                recruitCompleted,
                provisionAvailable
        );
    }
}