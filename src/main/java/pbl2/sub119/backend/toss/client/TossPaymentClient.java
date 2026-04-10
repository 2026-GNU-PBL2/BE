package pbl2.sub119.backend.toss.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.toss.dto.request.TossBillingAuthRequest;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;
import pbl2.sub119.backend.toss.dto.response.TossBillingAuthResponse;
import pbl2.sub119.backend.toss.dto.response.TossBillingPaymentResponse;
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

    public TossBillingAuthResponse issueBillingKey(TossBillingAuthRequest request) {
        validateBillingAuthRequest(request);

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

    public TossBillingPaymentResponse executeBillingPayment(
            String billingKey,
            TossBillingPaymentRequest request,
            String idempotencyKey
    ) {
        validateBillingPaymentRequest(billingKey, request, idempotencyKey);

        log.info("토스 자동결제 요청. orderId={}, amount={}",
                request.orderId(), request.amount());

        try {
            return webClient.post()
                    .uri(tossPaymentProperties.getBaseUrl() + "/v1/billing/" + billingKey)
                    .header(HttpHeaders.AUTHORIZATION, encodeSecretKey())
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossBillingPaymentResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("토스 자동결제 실패. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException(ErrorCode.PAYMENT_BILLING_EXECUTION_FAILED);
        }
    }

    private void validateBillingAuthRequest(TossBillingAuthRequest request) {
        if (request == null || isBlank(request.customerKey())) {
            throw new PaymentException(ErrorCode.PAYMENT_INVALID_BILLING_REQUEST);
        }
    }

    private void validateBillingPaymentRequest(
            String billingKey,
            TossBillingPaymentRequest request,
            String idempotencyKey
    ) {
        if (isBlank(billingKey)
                || request == null
                || isBlank(request.customerKey())
                || isBlank(request.orderId())
                || request.amount() == null
                || request.amount() <= 0) {
            throw new PaymentException(ErrorCode.PAYMENT_INVALID_BILLING_REQUEST);
        }

        if (isBlank(idempotencyKey)) {
            throw new PaymentException(ErrorCode.PAYMENT_INVALID_IDEMPOTENCY_KEY);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}