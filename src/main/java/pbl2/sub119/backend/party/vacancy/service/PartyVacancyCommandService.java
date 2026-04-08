package pbl2.sub119.backend.party.vacancy.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.exception.PartyException;
import pbl2.sub119.backend.party.join.service.PartyJoinService;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyJoinResponse;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.service.SubProductService;

@Service
@RequiredArgsConstructor
public class PartyVacancyCommandService {

    private final PartyMapper partyMapper;
    private final PartyJoinService partyJoinService;
    private final SubProductService subProductService;

    // 결원 파티 직접 참여
    @Transactional
    public PartyVacancyJoinResponse joinMemberVacancyParty(final Long partyId, final Long userId) {
        final Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }

        if (party.getCurrentMemberCount() >= party.getCapacity()) {
            throw new PartyException(ErrorCode.PARTY_FULL);
        }

        partyJoinService.joinParty(partyId, userId);

        final SubProductResponse product = subProductService.getProduct(party.getProductId());

        return new PartyVacancyJoinResponse(
                partyId,
                party.getProductId(),
                product.getServiceName(),
                LocalDateTime.now(),
                "결원 파티 참여가 완료되었습니다."
        );
    }
}