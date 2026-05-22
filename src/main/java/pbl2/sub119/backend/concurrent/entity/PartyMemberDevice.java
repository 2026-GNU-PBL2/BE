package pbl2.sub119.backend.concurrent.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pbl2.sub119.backend.concurrent.enumerated.RegistrationMethod;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyMemberDevice {

    private Long id;
    private Long userId;
    private Long partyId;
    private String deviceType;
    private String os;
    private String browser;
    private String ipLocation;
    private boolean vpn;
    private RegistrationMethod registrationMethod;
    private LocalDateTime registeredAt;
}
