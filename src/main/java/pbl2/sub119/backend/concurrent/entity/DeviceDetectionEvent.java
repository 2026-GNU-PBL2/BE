package pbl2.sub119.backend.concurrent.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.concurrent.enumerated.DeviceDetectionStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetectionEvent {

    private Long id;
    private Long partyId;
    private String detectedDevice;
    private String detectedLocation;
    private LocalDateTime detectedAt;
    private DeviceDetectionStatus status;
    private String notifiedUserIds;   // TEXT 컬럼, "[1,2,3]" JSON 배열 문자열
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
