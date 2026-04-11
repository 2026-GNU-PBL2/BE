package pbl2.sub119.backend.bankaccounts.service;

import org.springframework.stereotype.Component;
import pbl2.sub119.backend.bankaccounts.dto.BankAuthState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BankAuthStateStore {

    private final Map<String, BankAuthState> store = new ConcurrentHashMap<>();

    public String create(Long userId, Long productId) {
        String state = UUID.randomUUID().toString().replace("-", "");
        store.put(state, new BankAuthState(userId, productId));
        return state;
    }

    public BankAuthState get(String state) {
        return store.get(state);
    }

    public void remove(String state) {
        store.remove(state);
    }
}