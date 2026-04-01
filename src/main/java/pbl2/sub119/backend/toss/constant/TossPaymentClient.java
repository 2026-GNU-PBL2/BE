package pbl2.sub119.backend.toss.constant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.toss.constant.TossPaymentProperties;
import pbl2.sub119.backend.toss.dto.request.TossBillingAuthRequest;
import pbl2.sub119.backend.toss.dto.request.TossBillingChargeRequest;
import pbl2.sub119.backend.toss.dto.response.TossBillingAuthResponse;
import pbl2.sub119.backend.toss.dto.response.TossBillingChargeResponse;
import pbl2.sub119.backend.toss.exception.PaymentException;

import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final TossPaymentProperties tossPaymentProperties;
    private final WebClient webClient;

    private String encodeSecretKey() {
        String credentials = tossPaymentProperties.getSecretKey() + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * 빌링키 발급
     * POST /v1/billing/authorizations/issue
     */
    public TossBillingAuthResponse issueBillingKey(TossBillingAuthRequest request) {
        log.info("토스 빌링키 발급 요청. customerKey={}", request.customerKey());
        try {
            return webClient.post()
                    .uri(tossPaymentProperties.getBaseUrl() + "/v1/billing/authorizations/issue")
                    .header(HttpHeaders.AUTHORIZATION, encodeSecretKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossBillingAuthResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("토스 빌링키 발급 실패. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException(ErrorCode.PAYMENT_BILLING_KEY_ISSUE_FAILED);
        }
    }

    /**
     * 자동결제 실행
     * POST /v1/billing/{billingKey}
     */
    public TossBillingChargeResponse charge(String billingKey, TossBillingChargeRequest request) {
        log.info("토스 자동결제 실행. billingKey={}, amount={}", billingKey, request.amount());
        try {
            return webClient.post()
                    .uri(tossPaymentProperties.getBaseUrl() + "/v1/billing/" + billingKey)
                    .header(HttpHeaders.AUTHORIZATION, encodeSecretKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossBillingChargeResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("토스 자동결제 실패. billingKey={}, status={}, body={}",
                    billingKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException(ErrorCode.PAYMENT_CHARGE_FAILED);
        }
    }
}
