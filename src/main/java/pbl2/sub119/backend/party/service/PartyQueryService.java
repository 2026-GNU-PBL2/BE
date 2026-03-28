package pbl2.sub119.backend.party.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.party.dto.response.PartyDetailResponse;
import pbl2.sub119.backend.party.dto.response.PartyListResponse;
import pbl2.sub119.backend.party.dto.response.PartyMemberResponse;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class PartyQueryService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;

    @Transactional(readOnly = true)
    public PartyDetailResponse getPartyDetail(Long partyId) {
        Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new PartyException("존재하지 않는 파티입니다.");
        }

        List<PartyMemberResponse> memberResponses = partyMemberMapper.findMembersByPartyId(partyId)
                .stream()
                .map(this::toMemberResponse)
                .toList();

        return new PartyDetailResponse(
                party.getId(),
                party.getProductId(),
                party.getHostUserId(),
                party.getCapacity(),
                party.getCurrentMemberCount(),
                party.getRecruitStatus(),
                party.getOperationStatus(),
                party.getVacancyType(),
                party.getPricePerMemberSnapshot(),
                party.getCreatedAt(),
                party.getUpdatedAt(),
                party.getTerminatedAt(),
                memberResponses
        );
    }

    @Transactional(readOnly = true)
    public List<PartyListResponse> getPartiesByProduct(String productId) {
        return partyMapper.findByProductId(productId)
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    private PartyMemberResponse toMemberResponse(PartyMember member) {
        return new PartyMemberResponse(
                member.getId(),
                member.getUserId(),
                member.getRole(),
                member.getStatus()
        );
    }

    private PartyListResponse toListResponse(Party party) {
        return new PartyListResponse(
                party.getId(),
                party.getProductId(),
                party.getHostUserId(),
                party.getCapacity(),
                party.getCurrentMemberCount(),
                party.getRecruitStatus(),
                party.getOperationStatus(),
                party.getVacancyType()
        );
    }
}