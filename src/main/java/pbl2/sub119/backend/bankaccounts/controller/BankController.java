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

    // KFTC 인증 후 콜백되는 엔드포인트f
    @GetMapping("/callback")
    public String callback(@RequestParam String code, @RequestParam String scope) {
        // 실제로는 세션이나 토큰에서 userId를 가져와야 합니다.
        bankService.registerAccount(1L, code);
        return "계좌 등록 성공!";
    }
}
