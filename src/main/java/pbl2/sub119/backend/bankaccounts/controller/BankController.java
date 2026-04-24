package pbl2.sub119.backend.bankaccounts.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.bankaccounts.controller.docs.BankDocs;
import pbl2.sub119.backend.bankaccounts.dto.BankAuthState;
import pbl2.sub119.backend.bankaccounts.dto.request.RegisterSettlementAccountRequest;
import pbl2.sub119.backend.bankaccounts.dto.response.BankAccountSummaryResponse;
import pbl2.sub119.backend.bankaccounts.dto.response.BankAuthorizeUrlResponse;
import pbl2.sub119.backend.bankaccounts.dto.response.PrimaryBankAccountResponse;
import pbl2.sub119.backend.bankaccounts.service.BankAuthStateStore;
import pbl2.sub119.backend.bankaccounts.service.BankService;
import pbl2.sub119.backend.common.exception.BusinessException;

import java.net.URI;
import java.util.List;

import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED;

@RestController
@RequestMapping("/api/v1/bank")
@RequiredArgsConstructor
public class BankController implements BankDocs {

    private final BankService bankService;
    private final BankAuthStateStore bankAuthStateStore;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${kftc.client-id}")
    private String kftcClientId;

    @Value("${kftc.redirect-url}")
    private String kftcRedirectUrl;

    @Value("${kftc.base-url}")
    private String kftcBaseUrl;

    @Override
    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(
            @Auth final Accessor accessor,
            @RequestParam Long productId
    ) {
        String state = bankAuthStateStore.create(accessor.getUserId(), productId);

        String authorizeUrl = UriComponentsBuilder
                .fromHttpUrl(kftcBaseUrl + "/oauth/2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kftcClientId)
                .queryParam("redirect_uri", kftcRedirectUrl)
                .queryParam("scope", "login inquiry")
                .queryParam("state", state)
                .queryParam("auth_type", "0")
                .toUriString();

        return redirect(authorizeUrl);
    }

    @Override
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            @RequestParam(required = false) String scope,
            @RequestParam String state
    ) {
        BankAuthState authState = bankAuthStateStore.get(state);

        if (authState == null) {
            String invalidStateUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                    .path("/party/create/0/host/account-register")
                    .queryParam("bankAuthSuccess", false)
                    .queryParam("message", "유효하지 않은 인증 요청입니다.")
                    .toUriString();

            return redirect(invalidStateUrl);
        }

        String redirectBaseUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/party/create/{productId}/host/account-register")
                .buildAndExpand(authState.getProductId())
                .toUriString();

        try {
            bankService.registerAccount(authState.getUserId(), code);
            bankAuthStateStore.remove(state);

            String successUrl = UriComponentsBuilder.fromUriString(redirectBaseUrl)
                    .queryParam("bankAuthSuccess", true)
                    .toUriString();

            return redirect(successUrl);

        } catch (BusinessException e) {
            bankAuthStateStore.remove(state);

            String failUrl = UriComponentsBuilder.fromUriString(redirectBaseUrl)
                    .queryParam("bankAuthSuccess", false)
                    .queryParam("message", e.getErrorCode().getMessage())
                    .toUriString();

            return redirect(failUrl);

        } catch (Exception e) {
            bankAuthStateStore.remove(state);

            String failUrl = UriComponentsBuilder.fromUriString(redirectBaseUrl)
                    .queryParam("bankAuthSuccess", false)
                    .queryParam("message", BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED.getMessage())
                    .toUriString();

            return redirect(failUrl);
        }
    }

    @Override
    @PostMapping("/settlement")
    public String registerSettlementAccount(
            @Auth final Accessor accessor,
            @Valid @RequestBody RegisterSettlementAccountRequest request
    ) {
        bankService.registerSettlementAccount(accessor.getUserId(), request);
        return "Settlement account registration success";
    }

    @Override
    @GetMapping("/accounts")
    public List<BankAccountSummaryResponse> getAccounts(@Auth final Accessor accessor) {
        return bankService.getAccounts(accessor.getUserId());
    }

    @Override
    @GetMapping("/accounts/primary")
    public PrimaryBankAccountResponse getPrimaryAccount(@Auth final Accessor accessor) {
        return bankService.getPrimaryAccount(accessor.getUserId());
    }

    private ResponseEntity<Void> redirect(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @Override
    @GetMapping("/authorize-url")
    public BankAuthorizeUrlResponse authorizeUrl(
            @Auth final Accessor accessor,
            @RequestParam Long productId
    ) {
        String state = bankAuthStateStore.create(accessor.getUserId(), productId);

        String authorizeUrl = UriComponentsBuilder
                .fromHttpUrl(kftcBaseUrl + "/oauth/2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kftcClientId)
                .queryParam("redirect_uri", kftcRedirectUrl)
                .queryParam("scope", "login inquiry")
                .queryParam("state", state)
                .queryParam("auth_type", "0")
                .toUriString();

        return new BankAuthorizeUrlResponse(authorizeUrl);
    }
}