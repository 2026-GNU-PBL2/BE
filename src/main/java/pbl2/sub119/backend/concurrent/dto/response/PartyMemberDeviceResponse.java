package pbl2.sub119.backend.concurrent.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.concurrent.entity.PartyMemberDevice;
import pbl2.sub119.backend.concurrent.enumerated.RegistrationMethod;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyMemberDeviceResponse {

    private Long id;
    private Long userId;
    private String nickname;
    private String email;
    private String deviceType;
    private String os;
    private String browser;
    private String ipLocation;
    private boolean vpn;
    private RegistrationMethod registrationMethod;
    private LocalDateTime registeredAt;

    public static PartyMemberDeviceResponse from(final PartyMemberDevice device) {
        return PartyMemberDeviceResponse.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .deviceType(device.getDeviceType())
                .os(device.getOs())
                .browser(device.getBrowser())
                .ipLocation(device.getIpLocation())
                .vpn(device.isVpn())
                .registrationMethod(device.getRegistrationMethod())
                .registeredAt(device.getRegisteredAt())
                .build();
    }
}
