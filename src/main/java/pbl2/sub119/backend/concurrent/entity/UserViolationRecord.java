package pbl2.sub119.backend.concurrent.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.concurrent.enumerated.ViolationType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserViolationRecord {

    private Long id;
    private Long userId;
    private Long partyId;
    private Long incidentId;
    private ViolationType violationType;
    private BigDecimal weight;
    private LocalDateTime createdAt;
}
