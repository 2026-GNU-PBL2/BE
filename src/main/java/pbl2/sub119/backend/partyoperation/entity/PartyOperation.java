package pbl2.sub119.backend.partyoperation.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.partyoperation.enumerated.OperationStatus;
import pbl2.sub119.backend.partyoperation.enumerated.OperationType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyOperation {

    private Long id;
    private Long partyId;
    private OperationType operationType;
    private OperationStatus operationStatus;
    private String inviteValue;
    private String sharedAccountEmail;
    private String sharedAccountPasswordEncrypted;
    private String operationGuide;
    private LocalDateTime operationStartedAt;
    private LocalDateTime operationCompletedAt;
    private LocalDateTime lastResetAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}