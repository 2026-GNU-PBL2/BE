package pbl2.sub119.backend.concurrent.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CredentialResponse {

    private String sharedAccountEmail;
    private String sharedAccountPassword;
}
