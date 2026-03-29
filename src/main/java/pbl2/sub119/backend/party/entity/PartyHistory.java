package pbl2.sub119.backend.party.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyHistory {

    private Long id;
    private Long partyId;
    private Long memberId;
    private PartyHistoryEventType eventType;
    private String eventPayload;
    private LocalDateTime createdAt;
    private Long createdBy;
}