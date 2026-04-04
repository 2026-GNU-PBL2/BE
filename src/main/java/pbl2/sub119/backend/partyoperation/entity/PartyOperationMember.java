package pbl2.sub119.backend.partyoperation.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.partyoperation.enumerated.OperationMemberStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyOperationMember {

    private Long id;
    private Long partyOperationId;
    private Long partyMemberId;
    private Long partyId;
    private Long userId;
    private OperationMemberStatus memberStatus;
    private LocalDateTime inviteSentAt;
    private LocalDateTime mustCompleteBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime lastResetAt;
    private boolean penaltyApplied;
    private String operationMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}