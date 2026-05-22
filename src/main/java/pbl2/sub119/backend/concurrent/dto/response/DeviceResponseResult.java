package pbl2.sub119.backend.concurrent.dto.response;

import lombok.Builder;
import lombok.Getter;
import pbl2.sub119.backend.concurrent.enumerated.DeviceDetectionStatus;

@Getter
@Builder
public class DeviceResponseResult {

    private Long alertId;
    private DeviceDetectionStatus status;
    private int mineCount;
    private int unknownCount;
    private int responseCount;
}
