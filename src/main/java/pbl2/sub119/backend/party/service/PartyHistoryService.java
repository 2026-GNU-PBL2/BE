package pbl2.sub119.backend.party.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.party.entity.PartyHistory;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.mapper.PartyHistoryMapper;

@Service
@RequiredArgsConstructor
public class PartyHistoryService {

    private final PartyHistoryMapper partyHistoryMapper;

    public void saveHistory(
            Long partyId,
            Long memberId,
            PartyHistoryEventType eventType,
            String eventPayload,
            Long createdBy
    ) {
        PartyHistory history = PartyHistory.builder()
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