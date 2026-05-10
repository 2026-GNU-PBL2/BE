package pbl2.sub119.backend.bankaccounts.dto;

import lombok.Getter;
import pbl2.sub119.backend.bankaccounts.enums.BankAuthFlow;

@Getter
public class BankAuthState {
    private final Long userId;
    private final BankAuthFlow flow;
    private final String productId;

    private BankAuthState(Long userId, BankAuthFlow flow, String productId) {
        this.userId = userId;
        this.flow = flow;
        this.productId = productId;
    }

    public static BankAuthState forPartyCreate(Long userId, String productId) {
        return new BankAuthState(userId, BankAuthFlow.PARTY_CREATE, productId);
    }

    public static BankAuthState forMyPage(Long userId) {
        return new BankAuthState(userId, BankAuthFlow.MY_PAGE, null);
    }
}
