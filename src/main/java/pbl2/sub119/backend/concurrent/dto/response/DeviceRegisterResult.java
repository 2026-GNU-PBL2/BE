package pbl2.sub119.backend.concurrent.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import pbl2.sub119.backend.concurrent.enumerated.RegistrationMethod;

@Getter
@Builder
public class DeviceRegisterResult {

    private Long deviceId;
    private Long partyId;
    private String deviceType;
    private String os;
    private String browser;
    private RegistrationMethod registrationMethod;
    private LocalDateTime registeredAt;
}
