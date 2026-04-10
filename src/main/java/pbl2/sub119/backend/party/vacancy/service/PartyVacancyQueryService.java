package pbl2.sub119.backend.party.vacancy.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.vacancy.dto.response.MemberVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyDetailResponse;
import pbl2.sub119.backend.party.vacancy.mapper.PartyVacancyQueryMapper;

@Service
@RequiredArgsConstructor
public class PartyVacancyQueryService {

    private final PartyVacancyQueryMapper partyVacancyQueryMapper;

    // 파티원 결원 파티 목록 조회
    @Transactional(readOnly = true)
    public List<MemberVacancyPartyResponse> getMemberVacancyParties(final String productId) {
        return partyVacancyQueryMapper.findMemberVacancyParties(productId);
    }

    // 결원 파티 상세 조회
    @Transactional(readOnly = true)
    public PartyVacancyDetailResponse getMemberVacancyDetail(final Long partyId) {
        final PartyVacancyDetailResponse response = partyVacancyQueryMapper.findMemberVacancyDetail(partyId);
        if (response == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }
        return response;
    }
}