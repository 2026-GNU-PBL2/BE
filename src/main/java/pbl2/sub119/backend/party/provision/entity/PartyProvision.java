package pbl2.sub119.backend.party.provision.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyProvision {

    private Long id;
    private Long partyId;
    private ProvisionType operationType;
    private ProvisionStatus operationStatus;
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