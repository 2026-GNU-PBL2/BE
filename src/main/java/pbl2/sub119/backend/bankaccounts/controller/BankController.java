package pbl2.sub119.backend.bankaccounts.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.bankaccounts.service.BankService;

@RestController
@RequestMapping("/api/v1/bank")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping("/callback")
    public String callback(@RequestParam String code, @RequestParam String scope) {
        // User 붙으면 변경 (03.14)
        bankService.registerAccount(1L, code);
        return "계좌 등록 성공!";
    }
}
