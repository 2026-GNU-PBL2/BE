package pbl2.sub119.backend.concurrent.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import pbl2.sub119.backend.concurrent.enumerated.IncidentStatus;
import pbl2.sub119.backend.concurrent.enumerated.WarningLevel;

@Getter
@Builder
public class IncidentResult {

    private Long incidentId;
    private Long partyId;
    private WarningLevel warningLevel;
    private IncidentStatus status;
    private LocalDateTime hostDeadline;
    private LocalDate dissolutionDate;
}
