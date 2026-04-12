package pbl2.sub119.backend.bankaccounts.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import pbl2.sub119.backend.bankaccounts.config.KftcProperties;
import pbl2.sub119.backend.bankaccounts.dto.KftcAccountRealNameResponse;
import pbl2.sub119.backend.bankaccounts.dto.KftcTokenResponse;
import pbl2.sub119.backend.bankaccounts.dto.KftcUserInfoResponse;
import pbl2.sub119.backend.common.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_CONNECT_REQUEST_FAILED;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED;

@Slf4j
@Component
@RequiredArgsConstructor
public class KftcApiClient {

    private static final DateTimeFormatter TRAN_DTIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WebClient webClient;
    private final KftcProperties kftcProperties;
    private final ObjectMapper objectMapper;

    public KftcTokenResponse requestToken(String code) {
        try {
            String tokenJson = webClient.post()
                    .uri(kftcProperties.getBaseUrl() + "/oauth/2.0/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("code", code)
                            .with("client_id", kftcProperties.getClientId())
                            .with("client_secret", kftcProperties.getClientSecret())
                            .with("redirect_uri", kftcProperties.getRedirectUrl())
                            .with("grant_type", "authorization_code"))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(tokenJson, KftcTokenResponse.class);
        } catch (Exception e) {
            log.error("KFTC token request failed", e);
            throw new BusinessException(BANK_ACCOUNT_CONNECT_REQUEST_FAILED);
        }
    }

    public KftcUserInfoResponse requestUserInfo(KftcTokenResponse tokenResponse) {
        try {
            String userJson = webClient.get()
                    .uri(kftcProperties.getBaseUrl() + "/v2.0/user/me?user_seq_no=" + tokenResponse.getUserSeqNo())
                    .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(userJson, KftcUserInfoResponse.class);
        } catch (Exception e) {
            log.error("KFTC user info request failed", e);
            throw new BusinessException(BANK_ACCOUNT_CONNECT_REQUEST_FAILED);
        }
    }

    public KftcAccountRealNameResponse requestAccountRealName(
            String accessToken,
            String bankCode,
            String accountNumber,
            String accountHolderBirthDate,
            String bankTranId
    ) {
        try {
            String resolvedBankTranId = resolveBankTranId(bankTranId);
            String tranDtime = LocalDateTime.now().format(TRAN_DTIME_FORMAT);

            String uri = UriComponentsBuilder
                    .fromUriString(kftcProperties.getBaseUrl() + "/v2.0/inquiry/real_name")
                    .queryParam("bank_tran_id", resolvedBankTranId)
                    .queryParam("bank_code_std", bankCode)
                    .queryParam("account_num", accountNumber)
                    .queryParam("account_holder_info_type", " ")
                    .queryParam("account_holder_info", accountHolderBirthDate)
                    .queryParam("tran_dtime", tranDtime)
                    .toUriString();

            String responseJson = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(responseJson, KftcAccountRealNameResponse.class);
        } catch (Exception e) {
            log.error("KFTC real-name request failed", e);
            throw new BusinessException(BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED);
        }
    }

    private String resolveBankTranId(String bankTranId) {
        if (bankTranId != null && !bankTranId.isBlank()) {
            return bankTranId;
        }

        String orgCode = kftcProperties.getUseOrgCode();
        if (orgCode == null || orgCode.isBlank()) {
            orgCode = "M202600000";
        }

        String randomPart = String.format(
                "%011d",
                ThreadLocalRandom.current().nextLong(0, 100_000_000_000L)
        );

        String generated = orgCode + randomPart;
        return generated.length() > 20 ? generated.substring(0, 20) : generated;
    }
}