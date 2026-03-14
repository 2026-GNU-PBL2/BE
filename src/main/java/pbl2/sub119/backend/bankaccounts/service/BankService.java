package pbl2.sub119.backend.bankaccounts.service;

import com.fasterxml.jackson.databind.ObjectMapper; // 이거 import 필수
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class BankService {
    private final WebClient webClient;
    private final BankMapper bankMapper;
    private final KftcProperties kftcProperties;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 수동 변환기

    @Transactional
    public void registerAccount(Long userId, String code) {
        System.out.println("================ [DEBUGGING START] ================");

        // 1. 토큰 발급 (String으로 받기)
        String tokenJson = "";
        try {
            tokenJson = webClient.post()
                    .uri(kftcProperties.getBaseUrl() + "/oauth/2.0/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("code", code)
                            .with("client_id", kftcProperties.getClientId())
                            .with("client_secret", kftcProperties.getClientSecret())
                            .with("redirect_uri", kftcProperties.getRedirectUrl())
                            .with("grant_type", "authorization_code"))
                    .retrieve()
                    .bodyToMono(String.class) // DTO 말고 String으로 받음
                    .block();

            System.out.println(">> [RAW TOKEN JSON]: " + tokenJson); // 눈으로 확인

        } catch (Exception e) {
            System.out.println(">> [TOKEN ERROR]: " + e.getMessage());
            throw new RuntimeException("토큰 요청 실패");
        }

        // 2. 수동 매핑 (Jackson 동작 확인)
        KftcTokenResponse tokenResponse;
        try {
            tokenResponse = objectMapper.readValue(tokenJson, KftcTokenResponse.class);
            System.out.println(">> [MAPPED TOKEN]: " + tokenResponse.getAccessToken());
        } catch (Exception e) {
            System.out.println(">> [MAPPING ERROR]: DTO 매핑 실패 - " + e.getMessage());
            throw new RuntimeException("토큰 매핑 실패");
        }

        // 3. 사용자 조회 (String으로 받기)
        String userJson = "";
        try {
            userJson = webClient.get()
                    .uri(kftcProperties.getBaseUrl() + "/v2.0/user/me?user_seq_no=" + tokenResponse.getUserSeqNo())
                    .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(">> [RAW USER JSON]: " + userJson); // 눈으로 확인

        } catch (Exception e) {
            System.out.println(">> [USER INFO ERROR]: " + e.getMessage());
            return;
        }

        // 4. 수동 매핑 및 저장
        try {
            KftcUserInfoResponse userInfo = objectMapper.readValue(userJson, KftcUserInfoResponse.class);

            if (userInfo.getResList() != null) {
                System.out.println(">> [ACCOUNT COUNT]: " + userInfo.getResList().size());
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
                    bankMapper.saveBankAccount(bankAccount);
                    System.out.println(">> [DB INSERT]: 완료 - " + accountDto.getFintechUseNum());
                }
            } else {
                System.out.println(">> [ACCOUNT LIST IS NULL]");
            }
        } catch (Exception e) {
            System.out.println(">> [USER MAPPING ERROR]: " + e.getMessage());
        }
        System.out.println("================ [DEBUGGING END] ================");
    }
}