package pbl2.sub119.backend.party.history.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.history.dto.PartyHistoryResponse;
import pbl2.sub119.backend.party.history.mapper.PartyHistoryQueryMapper;

@Service
@RequiredArgsConstructor
public class PartyHistoryQueryService {

    private final PartyHistoryQueryMapper partyHistoryQueryMapper;

    // 내 파티 히스토리 조회
    @Transactional(readOnly = true)
    public List<PartyHistoryResponse> getMyPartyHistories(final Long userId) {
        if (userId == null) {
            throw new PartyException(ErrorCode.PARTY_INVALID_USER_ID);
        }

        return partyHistoryQueryMapper.findMyPartyHistories(userId);
    }
}