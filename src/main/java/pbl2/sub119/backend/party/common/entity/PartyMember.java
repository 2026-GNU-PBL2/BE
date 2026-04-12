package pbl2.sub119.backend.party.common.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyMember {

    private Long id;
    private Long partyId;
    private Long userId;
    private PartyRole role;
    private PartyMemberStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime serviceStartAt;
    private LocalDateTime serviceEndAt;
    private LocalDateTime leaveReservedAt;
    private LocalDateTime leftAt;
    private Long replacedTargetMemberId;
}