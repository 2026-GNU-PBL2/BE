package pbl2.sub119.backend.concurrent.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import pbl2.sub119.backend.concurrent.entity.PartyMemberDevice;

@Getter
@Builder
public class DeviceReportResult {

    private Long alertId;
    private Long partyId;
    private int notifiedCount;
    private LocalDateTime expiresAt;
    private List<RegisteredDeviceInfo> registeredDevices;

    @Getter
    @Builder
    public static class RegisteredDeviceInfo {
        private Long userId;
        private String deviceType;
        private String os;
        private String browser;

        public static RegisteredDeviceInfo from(final PartyMemberDevice device) {
            return RegisteredDeviceInfo.builder()
                    .userId(device.getUserId())
                    .deviceType(device.getDeviceType())
                    .os(device.getOs())
                    .browser(device.getBrowser())
                    .build();
        }
    }
}
