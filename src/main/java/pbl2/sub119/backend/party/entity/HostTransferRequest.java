package pbl2.sub119.backend.party.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.party.enumerated.HostTransferStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostTransferRequest {

    private Long id;
    private Long partyId;
    private Long requesterUserId;
    private Long targetUserId;
    private HostTransferStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}