package pbl2.sub119.backend.concurrent.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.concurrent.enumerated.DetectionSource;
import pbl2.sub119.backend.concurrent.enumerated.IncidentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentIncident {

    private Long id;
    private Long partyId;
    private Long reportedBy;
    private DetectionSource detectionSource;
    private String reportType;
    private IncidentStatus status;
    private LocalDateTime firstWarnedAt;
    private LocalDateTime hostDeadline;
    private LocalDate dissolutionDate;
    private LocalDateTime adminEscalatedAt;
    private LocalDateTime resolvedAt;
    private boolean webNotified;
    private boolean smsNotified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
