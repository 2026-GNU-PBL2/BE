package pbl2.sub119.backend.party.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pbl2.sub119.backend.party.enumerated.MatchWaitingStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MatchWaitingQueue {

    private Long id;
    private String productId;
    private Long userId;
    private MatchWaitingStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime matchedAt;
    private LocalDateTime canceledAt;
    private Long targetPartyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}