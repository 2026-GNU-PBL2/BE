package pbl2.sub119.backend.bankaccounts.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.bankaccounts.docs.BankDocs;
import pbl2.sub119.backend.bankaccounts.dto.request.RegisterSettlementAccountRequest;
import pbl2.sub119.backend.bankaccounts.dto.response.BankAccountSummaryResponse;
import pbl2.sub119.backend.bankaccounts.dto.response.PrimaryBankAccountResponse;
import pbl2.sub119.backend.bankaccounts.service.BankService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bank")
@RequiredArgsConstructor
public class BankController implements BankDocs {

    private final BankService bankService;

    @Override
    @GetMapping("/callback")
    public String callback(
            @Auth final Accessor accessor,
            @RequestParam String code,
            @RequestParam String scope
    ) {
        bankService.registerAccount(accessor.getUserId(), code);
        return "Account registration success";
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
}