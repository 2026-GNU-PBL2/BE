package pbl2.sub119.backend.party.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Party {

    private Long id;
    private String productId;
    private Long hostUserId;
    private Integer capacity;
    private Integer currentMemberCount;
    private RecruitStatus recruitStatus;
    private OperationStatus operationStatus;
    private VacancyType vacancyType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime terminatedAt;
    private Integer pricePerMemberSnapshot;
}