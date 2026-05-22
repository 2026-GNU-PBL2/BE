package pbl2.sub119.backend.concurrent.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyWarningHistory {

    private Long id;
    private Long partyId;
    private Long incidentId;
    private String level;
    private LocalDateTime createdAt;
}
