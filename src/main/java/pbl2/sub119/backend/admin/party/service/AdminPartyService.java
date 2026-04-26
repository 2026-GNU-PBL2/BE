package pbl2.sub119.backend.admin.party.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.admin.party.dto.AdminPartyDetailBaseResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyDetailResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyMemberResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyResponse;
import pbl2.sub119.backend.admin.party.mapper.AdminPartyMapper;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.exception.PartyException;

@Service
@RequiredArgsConstructor
public class AdminPartyService {

    private final AdminPartyMapper adminPartyMapper;

    // 파티 목록 조회
    public List<AdminPartyResponse> getParties() {
        return adminPartyMapper.findParties();
    }

    // 파티 상세 조회
    public AdminPartyDetailResponse getParty(final Long partyId) {
        final AdminPartyDetailBaseResponse party = adminPartyMapper.findPartyById(partyId);

        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        final List<AdminPartyMemberResponse> members = adminPartyMapper.findMembersByPartyId(partyId);

        return new AdminPartyDetailResponse(
                party.partyId(),
                party.displayPartyId(),
                party.productName(),
                party.hostNickname(),
                party.createdAt(),
                party.currentMemberCount(),
                party.maxMemberCount(),
                party.pricePerMember(),
                party.nextBillingDate(),
                party.recruitStatus(),
                party.operationStatus(),
                members
        );
    }
}