package pbl2.sub119.backend.bankaccounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.bankaccounts.client.KftcApiClient;
import pbl2.sub119.backend.bankaccounts.dto.KftcAccountRealNameResponse;
import pbl2.sub119.backend.bankaccounts.dto.KftcTokenResponse;
import pbl2.sub119.backend.bankaccounts.dto.KftcUserInfoResponse;
import pbl2.sub119.backend.bankaccounts.dto.request.RegisterSettlementAccountRequest;
import pbl2.sub119.backend.bankaccounts.dto.response.BankAccountSummaryResponse;
import pbl2.sub119.backend.bankaccounts.dto.response.PrimaryBankAccountResponse;
import pbl2.sub119.backend.bankaccounts.entity.BankAccount;
import pbl2.sub119.backend.bankaccounts.enums.VerificationStatus;
import pbl2.sub119.backend.bankaccounts.mapper.BankMapper;
import pbl2.sub119.backend.common.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;

import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_VERIFICATION_FAILED;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_CONNECTED_ACCOUNT_NOT_FOUND;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_PRIMARY_ACCOUNT_NOT_FOUND;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_SETTLEMENT_ACCOUNT_REGISTER_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    private final BankMapper bankMapper;
    private final KftcApiClient kftcApiClient;

    @Transactional
    public void registerAccount(Long userId, String code) {
        log.info("KFTC account registration started. userId={}", userId);

        KftcTokenResponse tokenResponse = kftcApiClient.requestToken(code);
        KftcUserInfoResponse userInfo = kftcApiClient.requestUserInfo(tokenResponse);

        if (userInfo.getResList() == null || userInfo.getResList().isEmpty()) {
            log.warn("KFTC account list is empty. userId={}", userId);
            return;
        }

        int processedCount = 0;

        for (KftcUserInfoResponse.KftcAccountDto accountDto : userInfo.getResList()) {
            BankAccount bankAccount = BankAccount.builder()
                    .userId(userId)
                    .fintechUseNum(accountDto.getFintechUseNum())
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .bankName(accountDto.getBankName())
                    .accountAlias(accountDto.getAccountAlias())
                    .accountNumMasked(accountDto.getAccountNumMasked())
                    .balanceAmt(0L)
                    .build();

            boolean exists = bankMapper.existsByUserIdAndFintechUseNum(userId, accountDto.getFintechUseNum());

            if (exists) {
                bankMapper.updateBankAccount(bankAccount);
            } else {
                bankMapper.saveBankAccount(bankAccount);
            }

            processedCount++;
        }

        log.info("KFTC account registration completed. userId={}, accountCount={}", userId, processedCount);
    }

    @Transactional
    public void registerSettlementAccount(Long userId, RegisterSettlementAccountRequest request) {
        BankAccount connectedAccount = bankMapper.findByUserIdAndFintechUseNum(userId, request.getFintechUseNum());
        if (connectedAccount == null) {
            throw new BusinessException(BANK_CONNECTED_ACCOUNT_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isPrimary = Boolean.TRUE.equals(request.getIsPrimary());

        if (isPrimary) {
            bankMapper.clearPrimaryByUserId(userId);
        }

        BankAccount bankAccount = BankAccount.builder()
                .userId(userId)
                .fintechUseNum(request.getFintechUseNum())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .accountHolderBirthDate(request.getAccountHolderBirthDate())
                .accountType(request.getAccountType())
                .isPrimary(isPrimary)
                .verificationStatus(VerificationStatus.PENDING)
                .verifiedAt(null)
                .lastVerifiedAt(now)
                .failReason(null)
                .updatedAt(now)
                .build();

        int updated = bankMapper.updateSettlementAccountMeta(bankAccount);
        if (updated != 1) {
            throw new BusinessException(BANK_SETTLEMENT_ACCOUNT_REGISTER_FAILED);
        }

        try {
            KftcAccountRealNameResponse realNameResponse = kftcApiClient.requestAccountRealName(
                    connectedAccount.getAccessToken(),
                    request.getBankCode(),
                    request.getAccountNumber(),
                    request.getAccountHolderBirthDate(),
                    connectedAccount.getBankTranId()
            );

            boolean nameMatched = normalizeName(request.getAccountHolderName())
                    .equals(normalizeName(realNameResponse.getAccountHolderName()));
            boolean verified = realNameResponse.isSuccess() && nameMatched;

            if (verified) {
                bankMapper.updateVerificationSuccess(userId, request.getFintechUseNum());
                log.info("Settlement account verified. userId={}, fintechUseNum={}", userId, request.getFintechUseNum());
                return;
            }

            String failReason = realNameResponse.getRspMessage();
            if (!nameMatched) {
                failReason = "예금주명이 일치하지 않습니다.";
            }

            bankMapper.updateVerificationFailure(
                    userId,
                    request.getFintechUseNum(),
                    trimFailReason(failReason)
            );

            log.warn("Settlement account verification failed. userId={}, fintechUseNum={}",
                    userId, request.getFintechUseNum());

            throw new BusinessException(BANK_ACCOUNT_VERIFICATION_FAILED);

        } catch (BusinessException e) {
            if (e.getErrorCode() == BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED) {
                bankMapper.updateVerificationFailure(
                        userId,
                        request.getFintechUseNum(),
                        trimFailReason(e.getErrorCode().getMessage())
                );
            }
            throw e;
        } catch (Exception e) {
            bankMapper.updateVerificationFailure(
                    userId,
                    request.getFintechUseNum(),
                    trimFailReason(e.getMessage())
            );
            throw new BusinessException(BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public List<BankAccountSummaryResponse> getAccounts(Long userId) {
        return bankMapper.findAllByUserId(userId).stream()
                .map(BankAccountSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrimaryBankAccountResponse getPrimaryAccount(Long userId) {
        BankAccount primary = bankMapper.findPrimaryByUserId(userId);
        if (primary == null) {
            throw new BusinessException(BANK_PRIMARY_ACCOUNT_NOT_FOUND);
        }
        return PrimaryBankAccountResponse.from(primary);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.replace(" ", "").trim();
    }

    private String trimFailReason(String failReason) {
        if (failReason == null || failReason.isBlank()) {
            return "계좌 검증에 실패했습니다.";
        }
        return failReason.length() > 250 ? failReason.substring(0, 250) : failReason;
    }
}