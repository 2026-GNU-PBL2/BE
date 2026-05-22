package pbl2.sub119.backend.concurrent.dto.response;

import lombok.Builder;
import lombok.Getter;
import pbl2.sub119.backend.concurrent.enumerated.IncidentStatus;

@Getter
@Builder
public class ResolveResult {

    private Long incidentId;
    private IncidentStatus status;
}
