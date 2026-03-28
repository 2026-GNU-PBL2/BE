package pbl2.sub119.backend.party.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.party.dto.request.PartyCreateRequest;
import pbl2.sub119.backend.party.dto.response.PartyCreateResponse;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.entity.PartyMember;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class PartyCommandService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryService partyHistoryService;

    @Transactional
    public PartyCreateResponse createParty(Long hostUserId, PartyCreateRequest request) {
        validateCreateRequest(request);

        Party party = Party.builder()
                .productId(request.productId())
                .hostUserId(hostUserId)
                .capacity(request.capacity())
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .pricePerMemberSnapshot(request.pricePerMemberSnapshot())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .terminatedAt(null)
                .build();

        partyMapper.insertParty(party);

        PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        partyHistoryService.saveHistory(
                party.getId(),
                hostMember.getId(),
                PartyHistoryEventType.PARTY_CREATED,
                "{\"hostUserId\":" + hostUserId + ",\"capacity\":" + request.capacity() + "}",
                hostUserId
        );

        return new PartyCreateResponse(
                party.getId(),
                party.getProductId(),
                party.getHostUserId(),
                party.getCapacity(),
                party.getCurrentMemberCount(),
                party.getRecruitStatus(),
                party.getOperationStatus(),
                party.getVacancyType(),
                party.getPricePerMemberSnapshot()
        );
    }

    private void validateCreateRequest(PartyCreateRequest request) {
        if (request.productId() == null) {
            throw new PartyException("상품 ID는 필수입니다.");
        }
        if (request.capacity() == null || request.capacity() < 2) {
            throw new PartyException("정원은 2명 이상이어야 합니다.");
        }
        if (request.pricePerMemberSnapshot() == null || request.pricePerMemberSnapshot() < 0) {
            throw new PartyException("1인당 금액 정보가 올바르지 않습니다.");
        }
    }
}