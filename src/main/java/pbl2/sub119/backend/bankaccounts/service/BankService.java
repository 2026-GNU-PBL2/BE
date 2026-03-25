package pbl2.sub119.backend.bankaccounts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pbl2.sub119.backend.bankaccounts.config.KftcProperties;
import pbl2.sub119.backend.bankaccounts.dto.KftcTokenResponse;
import pbl2.sub119.backend.bankaccounts.dto.KftcUserInfoResponse;
import pbl2.sub119.backend.bankaccounts.entity.BankAccount;
import pbl2.sub119.backend.bankaccounts.mapper.BankMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    private final WebClient webClient;
    private final BankMapper bankMapper;
    private final KftcProperties kftcProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public void registerAccount(Long userId, String code) {
        log.info("KFTC 계좌 인증 시작  userId={}", userId);

        KftcTokenResponse tokenResponse = requestToken(code);
        KftcUserInfoResponse userInfo = requestUserInfo(tokenResponse);

        if (userInfo.getResList() == null || userInfo.getResList().isEmpty()) {
            log.warn("KFTC 계좌 목록이 비어있음. userId={}", userId);
            return;
        }

        int processedCount = 0;

        for (KftcUserInfoResponse.KftcAccountDto accountDto : userInfo.getResList()) {
            BankAccount bankAccount = BankAccount.builder()
                    .userId(userId)
                    .fintechUseNum(accountDto.getFintechUseNum())
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .bankName(accountDto.getBankName())
                    .accountAlias(accountDto.getAccountAlias())
                    .accountNumMasked(accountDto.getAccountNumMasked())
                    .balanceAmt(0L)
                    .build();

            boolean exists = bankMapper.existsByUserIdAndFintechUseNum(userId, accountDto.getFintechUseNum());

            if (exists) {
                bankMapper.updateBankAccount(bankAccount);
            } else {
                bankMapper.saveBankAccount(bankAccount);
            }

            processedCount++;
        }

        log.info("KFTC 계좌 인증 완료. userId={}, accountCount={}", userId, processedCount);
    }

    private KftcTokenResponse requestToken(String code) {
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
            log.error("KFTC 토큰 요청 실패", e);
            throw new RuntimeException("오픈뱅킹 토큰 요청 실패", e);
        }
    }

    private KftcUserInfoResponse requestUserInfo(KftcTokenResponse tokenResponse) {
        try {
            String userJson = webClient.get()
                    .uri(kftcProperties.getBaseUrl() + "/v2.0/user/me?user_seq_no=" + tokenResponse.getUserSeqNo())
                    .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(userJson, KftcUserInfoResponse.class);
        } catch (Exception e) {
            log.error("KFTC 사용자 정보 조회 실패", e);
            throw new RuntimeException("오픈뱅킹 사용자 정보 조회 실패", e);
        }
    }
}