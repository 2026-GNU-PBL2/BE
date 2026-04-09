package pbl2.sub119.backend.party.provision.dto.request;

import jakarta.validation.constraints.Size;

// 파티장이 이용 재설정할 때 전달하는 안내 문구
public record PartyProvisionResetRequest(
        @Size(max = 500)
        String provisionMessage
) {
}