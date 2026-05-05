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
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_SIZE);

        long offsetLong = (long) normalizedPage * normalizedSize;
        if (offsetLong > Integer.MAX_VALUE) {
            offsetLong = Integer.MAX_VALUE; // 또는 예외 처리
        }

        int offset = (int) offsetLong;
        return userPaymentHistoryQueryMapper.findByUserId(userId, normalizedSize, offset);
    }
}
