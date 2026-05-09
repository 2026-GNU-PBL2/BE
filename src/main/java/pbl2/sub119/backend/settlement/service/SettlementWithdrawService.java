package pbl2.sub119.backend.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.bankaccounts.entity.BankAccount;
import pbl2.sub119.backend.bankaccounts.enums.AccountType;
import pbl2.sub119.backend.bankaccounts.enums.VerificationStatus;
import pbl2.sub119.backend.bankaccounts.mapper.BankMapper;
import pbl2.sub119.backend.common.enumerated.WithdrawRequestStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.pointWallet.entity.PointWallet;
import pbl2.sub119.backend.pointWallet.mapper.PointWalletMapper;
import pbl2.sub119.backend.settlement.dto.response.PointBalanceResponse;
import pbl2.sub119.backend.settlement.dto.response.SettlementHistoryResponse;
import pbl2.sub119.backend.settlement.dto.response.WithdrawRequestResponse;
import pbl2.sub119.backend.settlement.entity.PointWithdrawRequest;
import pbl2.sub119.backend.settlement.mapper.PointWithdrawRequestMapper;
import pbl2.sub119.backend.settlement.mapper.SettlementMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementWithdrawService {

    private static final long MIN_AMOUNT = 10_000L;
    private static final long MAX_AMOUNT = 100_000L;
    private static final String PAYOUT_REF_PREFIX = "PWR-";
    private static final DateTimeFormatter PAYOUT_REF_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String PAYOUT_REF_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final PointWithdrawRequestMapper pointWithdrawRequestMapper;
    private final PointWalletMapper pointWalletMapper;
    private final BankMapper bankMapper;
    private final SettlementMapper settlementMapper;

    @Transactional
    public WithdrawRequestResponse createWithdrawRequest(Long userId, Long amount) {
        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            throw new BusinessException(ErrorCode.SETTLEMENT_WITHDRAW_AMOUNT_INVALID);
        }

        BankAccount account = bankMapper.findPrimaryByUserId(userId);
        if (account == null
                || account.getAccountType() != AccountType.SETTLEMENT
                || account.getVerificationStatus() != VerificationStatus.VERIFIED
                || account.getBankName() == null
                || account.getAccountNumMasked() == null) {
            throw new BusinessException(ErrorCode.SETTLEMENT_WITHDRAW_ACCOUNT_NOT_FOUND);
        }

        int decreased = pointWalletMapper.decreaseBalanceIfEnough(userId, amount);
        if (decreased == 0) {
            throw new BusinessException(ErrorCode.SETTLEMENT_WITHDRAW_BALANCE_INSUFFICIENT);
        }

        LocalDateTime now = LocalDateTime.now();
        PointWithdrawRequest request = PointWithdrawRequest.builder()
                .userId(userId)
                .amount(amount)
                .status(WithdrawRequestStatus.REQUESTED)
                .bankAccountId(account.getId())
                .bankNameSnapshot(account.getBankName())
                .accountMaskedSnapshot(account.getAccountNumMasked())
                .internalPayoutRef(generateInternalPayoutRef(userId, now))
                .requestedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        pointWithdrawRequestMapper.insert(request);

        log.info("환급 요청 생성. userId={}, amount={}, requestId={}, internalPayoutRef={}",
                userId, amount, request.getId(), request.getInternalPayoutRef());
        return WithdrawRequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public List<WithdrawRequestResponse> getMyWithdrawRequests(Long userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = clampOffset(page, safeSize);
        return pointWithdrawRequestMapper.findByUserId(userId, safeSize, offset)
                .stream()
                .map(WithdrawRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PointBalanceResponse getMyPointBalance(Long userId) {
        PointWallet wallet = pointWalletMapper.findByUserId(userId);
        return PointBalanceResponse.of(wallet == null ? null : wallet.getBalance());
    }

    @Transactional(readOnly = true)
    public List<SettlementHistoryResponse> getMySettlementHistory(Long userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = clampOffset(page, safeSize);
        return settlementMapper.findByHostUserId(userId, safeSize, offset)
                .stream()
                .map(SettlementHistoryResponse::from)
                .toList();
    }

    private static String generateInternalPayoutRef(Long userId, LocalDateTime now) {
        String random = ThreadLocalRandom.current()
                .ints(6, 0, PAYOUT_REF_CHARS.length())
                .mapToObj(i -> String.valueOf(PAYOUT_REF_CHARS.charAt(i)))
                .reduce("", String::concat);
        return PAYOUT_REF_PREFIX + now.format(PAYOUT_REF_TS) + "-" + userId + "-" + random;
    }

    private static int clampOffset(int page, int safeSize) {
        int normalizedPage = page <= 1 ? 0 : page - 1;
        long offsetLong = (long) normalizedPage * safeSize;
        return (int) Math.min(offsetLong, Integer.MAX_VALUE);
    }
}
