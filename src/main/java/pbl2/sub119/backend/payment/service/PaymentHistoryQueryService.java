package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.payment.dto.response.PaymentHistoryItem;
import pbl2.sub119.backend.payment.mapper.UserPaymentHistoryQueryMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentHistoryQueryService {

    private static final int MAX_SIZE = 100;

    private final UserPaymentHistoryQueryMapper userPaymentHistoryQueryMapper;

    @Transactional(readOnly = true)
    public List<PaymentHistoryItem> getMyPaymentHistory(Long userId, int page, int size) {
        int normalizedSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int offset = Math.max(page, 0) * normalizedSize;
        return userPaymentHistoryQueryMapper.findByUserId(userId, normalizedSize, offset);
    }
}
