package pbl2.sub119.backend.party.common.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.party.common.entity.PartyHistory;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.mapper.PartyHistoryMapper;

@Service
@RequiredArgsConstructor
public class PartyHistoryService {

    private final PartyHistoryMapper partyHistoryMapper;

    // 파티 이력 저장
    public void saveHistory(
            final Long partyId,
            final Long memberId,
            final PartyHistoryEventType eventType,
            final String eventPayload,
            final Long createdBy
    ) {
        final PartyHistory history = PartyHistory.builder()
                .partyId(partyId)
                .memberId(memberId)
                .eventType(eventType)
                .eventPayload(eventPayload)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        partyHistoryMapper.insertHistory(history);
    }
}