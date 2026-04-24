package pbl2.sub119.backend.bankaccounts.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오픈뱅킹 인증 URL 응답")
public record BankAuthorizeUrlResponse(

        @Schema(
                description = "금융결제원 오픈뱅킹 authorize URL",
                example = "https://testapi.openbanking.or.kr/oauth/2.0/authorize?response_type=code&client_id=..."
        )
        String authorizeUrl
) {
}