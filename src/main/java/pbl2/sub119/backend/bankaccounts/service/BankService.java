package pbl2.sub119.backend.bankaccounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.bankaccounts.client.KftcApiClient;
import pbl2.sub119.backend.bankaccounts.dto.BankCandidateDto;
import pbl2.sub119.backend.bankaccounts.dto.KftcAccountRealNameResponse;
import pbl2.sub119.backend.bankaccounts.dto.KftcTokenResponse;
import pbl2.sub119.backend.bankaccounts.dto.KftcUserInfoResponse;
import pbl2.sub119.backend.bankaccounts.dto.request.RegisterSettlementAccountRequest;
import pbl2.sub119.backend.bankaccounts.dto.response.BankAccountSummaryResponse;
import pbl2.sub119.backend.bankaccounts.dto.response.PrimaryBankAccountResponse;
import pbl2.sub119.backend.bankaccounts.entity.BankAccount;
import pbl2.sub119.backend.bankaccounts.enums.AccountType;
import pbl2.sub119.backend.bankaccounts.enums.VerificationStatus;
import pbl2.sub119.backend.bankaccounts.mapper.BankMapper;
import pbl2.sub119.backend.common.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_VERIFICATION_FAILED;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_CONNECTED_ACCOUNT_NOT_FOUND;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_INVALID_ACCOUNT_TYPE;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_PRIMARY_ACCOUNT_NOT_FOUND;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_SETTLEMENT_ACCOUNT_REGISTER_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    private final BankMapper bankMapper;
    private final KftcApiClient kftcApiClient;
    private final BankCandidateStore bankCandidateStore;

    public void registerAccount(Long userId, String code) {
        log.info("KFTC account registration started. userId={}", userId);

        KftcTokenResponse tokenResponse = kftcApiClient.requestToken(code);
        KftcUserInfoResponse userInfo = kftcApiClient.requestUserInfo(tokenResponse);

        if (userInfo.getResList() == null || userInfo.getResList().isEmpty()) {
            log.warn("KFTC account list is empty. userId={}", userId);
            bankCandidateStore.save(userId, List.of());
            return;
        }

        List<BankCandidateDto> candidates = userInfo.getResList().stream()
                .map(dto -> BankCandidateDto.builder()
                        .fintechUseNum(dto.getFintechUseNum())
                        .bankTranId(dto.getBankTranId())
                        .bankName(dto.getBankName())
                        .accountAlias(dto.getAccountAlias())
                        .accountNumMasked(dto.getAccountNumMasked())
                        .accessToken(tokenResponse.getAccessToken())
                        .refreshToken(tokenResponse.getRefreshToken())
                        .build())
                .toList();

        bankCandidateStore.save(userId, candidates);
        log.info("KFTC account candidates cached. userId={}, accountCount={}", userId, candidates.size());
    }

    @Transactional
    public void registerSettlementAccount(Long userId, RegisterSettlementAccountRequest request) {
        if (request.getAccountType() != AccountType.SETTLEMENT) {
            throw new BusinessException(BANK_INVALID_ACCOUNT_TYPE);
        }

        Optional<BankCandidateDto> candidateOpt = bankCandidateStore.findByFintechUseNum(userId, request.getFintechUseNum());

        if (candidateOpt.isPresent()) {
            // 최신 인증 경로: Redis 후보에서 KFTC 메타 확보 후 DB row 보장
            ensureKftcRowExists(userId, candidateOpt.get());
        } else {
            // Redis 미스(TTL 만료 등): DB fallback
            BankAccount connectedAccount = bankMapper.findByUserIdAndFintechUseNum(userId, request.getFintechUseNum());
            if (connectedAccount == null) {
                throw new BusinessException(BANK_CONNECTED_ACCOUNT_NOT_FOUND);
            }
        }

        // 기존 활성 정산계좌 비활성화 (account_type/is_primary 초기화)
        bankMapper.deactivateSettlementAccountsByUserId(userId);

        LocalDateTime now = LocalDateTime.now();

        BankAccount bankAccount = BankAccount.builder()
                .userId(userId)
                .fintechUseNum(request.getFintechUseNum())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .accountHolderBirthDate(request.getAccountHolderBirthDate())
                .accountType(AccountType.SETTLEMENT)
                .isPrimary(true)
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

        //실명검증 테스트베드
//        try {
//            KftcAccountRealNameResponse realNameResponse = kftcApiClient.requestAccountRealName(
//                    connectedAccount.getAccessToken(),
//                    request.getBankCode(),
//                    request.getAccountNumber(),
//                    request.getAccountHolderBirthDate(),
//                    connectedAccount.getBankTranId()
//            );
//            if (!realNameResponse.isSuccess()) {
//                String failReason = trimFailReason(realNameResponse.getRspMessage());
//
//                bankMapper.updateVerificationFailure(
//                        userId,
//                        request.getFintechUseNum(),
//                        failReason
//                );
//
//                throw new BusinessException(BANK_ACCOUNT_VERIFICATION_FAILED);
//            }
//
//            boolean nameMatched = normalizeName(request.getAccountHolderName())
//                    .equals(normalizeName(realNameResponse.getAccountHolderName()));
//            boolean verified = realNameResponse.isSuccess() && nameMatched;
//
//            if (verified) {
//                bankMapper.updateVerificationSuccess(userId, request.getFintechUseNum());
//                log.info("Settlement account verified. userId={}, fintechUseNum={}", userId, request.getFintechUseNum());
//                bankCandidateStore.remove(userId);
//                return;
//            }
//
//            String failReason = realNameResponse.getRspMessage();
//            if (!nameMatched) {
//                failReason = "예금주명이 일치하지 않습니다.";
//            }
//
//            bankMapper.updateVerificationFailure(
//                    userId,
//                    request.getFintechUseNum(),
//                    trimFailReason(failReason)
//            );
//
//            log.warn("Settlement account verification failed. userId={}, fintechUseNum={}",
//                    userId, request.getFintechUseNum());
//
//            throw new BusinessException(BANK_ACCOUNT_VERIFICATION_FAILED);
//
//        } catch (BusinessException e) {
//            if (e.getErrorCode() == BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED) {
//                bankMapper.updateVerificationFailure(
//                        userId,
//                        request.getFintechUseNum(),
//                        trimFailReason(e.getErrorCode().getMessage())
//                );
//            }
//            throw e;
//        } catch (Exception e) {
//            bankMapper.updateVerificationFailure(
//                    userId,
//                    request.getFintechUseNum(),
//                    trimFailReason(e.getMessage())
//            );
//            throw new BusinessException(BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED);
//        }
        bankMapper.updateVerificationSuccess(userId, request.getFintechUseNum());
        bankCandidateStore.remove(userId);
    }

    @Transactional
    public void deactivateSettlementAccount(Long userId) {
        BankAccount primary = bankMapper.findPrimaryByUserId(userId);
        if (primary == null) {
            throw new BusinessException(BANK_PRIMARY_ACCOUNT_NOT_FOUND);
        }
        bankMapper.deactivateSettlementAccountsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<BankAccountSummaryResponse> getAccounts(Long userId) {
        List<BankCandidateDto> candidates = bankCandidateStore.findAll(userId);
        if (candidates != null) {
            return candidates.stream()
                    .map(BankAccountSummaryResponse::fromCandidate)
                    .toList();
        }
        log.warn("Bank candidate cache miss — falling back to DB. userId={}. Shown accounts may not reflect latest KFTC auth.", userId);
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

    private void ensureKftcRowExists(Long userId, BankCandidateDto candidate) {
        BankAccount kftcRow = BankAccount.builder()
                .userId(userId)
                .fintechUseNum(candidate.getFintechUseNum())
                .bankTranId(candidate.getBankTranId())
                .accessToken(candidate.getAccessToken())
                .refreshToken(candidate.getRefreshToken())
                .bankName(candidate.getBankName())
                .accountAlias(candidate.getAccountAlias())
                .accountNumMasked(candidate.getAccountNumMasked())
                .balanceAmt(0L)
                .build();
        if (bankMapper.existsByUserIdAndFintechUseNum(userId, candidate.getFintechUseNum())) {
            bankMapper.updateBankAccount(kftcRow);
        } else {
            bankMapper.saveBankAccount(kftcRow);
        }
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
