package pbl2.sub119.backend.concurrent.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetectionResponse {

    private Long id;
    private Long detectionEventId;
    private Long userId;
    private boolean mine;
    private LocalDateTime respondedAt;
}
