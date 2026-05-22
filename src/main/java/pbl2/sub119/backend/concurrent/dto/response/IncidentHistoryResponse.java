package pbl2.sub119.backend.concurrent.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import pbl2.sub119.backend.concurrent.entity.ConcurrentIncident;
import pbl2.sub119.backend.concurrent.enumerated.IncidentStatus;

@Getter
@Builder
public class IncidentHistoryResponse {

    private Long incidentId;
    private IncidentStatus status;
    private String detectionSource;
    private LocalDateTime firstWarnedAt;
    private LocalDateTime hostDeadline;
    private LocalDate dissolutionDate;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;

    public static IncidentHistoryResponse from(final ConcurrentIncident incident) {
        return IncidentHistoryResponse.builder()
                .incidentId(incident.getId())
                .status(incident.getStatus())
                .detectionSource(incident.getDetectionSource().name())
                .firstWarnedAt(incident.getFirstWarnedAt())
                .hostDeadline(incident.getHostDeadline())
                .dissolutionDate(incident.getDissolutionDate())
                .resolvedAt(incident.getResolvedAt())
                .createdAt(incident.getCreatedAt())
                .build();
    }

    public static List<IncidentHistoryResponse> fromList(final List<ConcurrentIncident> incidents) {
        return incidents.stream().map(IncidentHistoryResponse::from).toList();
    }
}
