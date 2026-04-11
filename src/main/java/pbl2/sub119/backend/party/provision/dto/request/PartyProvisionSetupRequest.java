package pbl2.sub119.backend.party.provision.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;

// 파티장이 이용 정보 등록할 때 전달하는 값
public record PartyProvisionSetupRequest(
        @NotNull
        ProvisionType provisionType,

        @Size(max = 500)
        String inviteValue,

        @Email
        @Size(max = 150)
        String sharedAccountEmail,

        @Size(max = 255)
        String sharedAccountPassword,

        @Size(max = 1000)
        String provisionGuide
) {
}