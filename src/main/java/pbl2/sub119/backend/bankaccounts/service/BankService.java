package pbl2.sub119.backend.bankaccounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.bankaccounts.client.KftcApiClient;
import pbl2.sub119.backend.bankaccounts.dto.BankAuthTokenDto;
import pbl2.sub119.backend.bankaccounts.dto.BankCandidateDto;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static pbl2.sub119.backend.common.error.ErrorCode.BANK_ACCOUNT_CONNECT_REQUEST_FAILED;
import static pbl2.sub119.backend.common.error.ErrorCode.BANK_AUTH_EXPIRED;
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
    private final BankAuthTokenStore bankAuthTokenStore;
    private final BankSelectedStore bankSelectedStore;

    public void registerAccount(Long userId, String code) {
        log.info("KFTC account registration started. userId={}", userId);

        KftcTokenResponse tokenResponse = kftcApiClient.requestToken(code);
        KftcUserInfoResponse userInfo = kftcApiClient.requestUserInfo(tokenResponse);

        // Ensure each callback starts from a clean snapshot.
        bankCandidateStore.remove(userId);
        bankAuthTokenStore.remove(userId);
        bankSelectedStore.remove(userId);

        boolean tokenSaved = bankAuthTokenStore.save(
                userId,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getUserSeqNo()
        );
        if (!tokenSaved) {
            throw new BusinessException(BANK_ACCOUNT_CONNECT_REQUEST_FAILED);
        }

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

        candidates = sanitizeCandidates(candidates);
        bankCandidateStore.save(userId, candidates);
        if (!candidates.isEmpty()) {
            bankSelectedStore.add(userId, candidates.get(0).getFintechUseNum());
        }
        log.info("KFTC account candidates cached. userId={}, accountCount={}", userId, candidates.size());
    }

    @Transactional
    public void registerSettlementAccount(Long userId, RegisterSettlementAccountRequest request) {
        if (request.getAccountType() != AccountType.SETTLEMENT) {
            throw new BusinessException(BANK_INVALID_ACCOUNT_TYPE);
        }

        Optional<BankCandidateDto> candidateOpt =
                bankCandidateStore.findByFintechUseNum(userId, request.getFintechUseNum());

        if (candidateOpt.isPresent()) {
            ensureKftcRowExists(userId, candidateOpt.get());
        } else {
            List<BankCandidateDto> recovered = recoverCandidates(userId);
            if (recovered == null) {
                throw new BusinessException(BANK_AUTH_EXPIRED);
            }

            Optional<BankCandidateDto> recoveredOpt = recovered.stream()
                    .filter(c -> request.getFintechUseNum().equals(c.getFintechUseNum()))
                    .findFirst();

            if (recoveredOpt.isEmpty()) {
                throw new BusinessException(BANK_AUTH_EXPIRED);
            }

            ensureKftcRowExists(userId, recoveredOpt.get());
            bankCandidateStore.save(userId, recovered);
        }

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

        bankMapper.updateVerificationSuccess(userId, request.getFintechUseNum());

        bankSelectedStore.remove(userId);
        bankSelectedStore.add(userId, request.getFintechUseNum());
    }

    @Transactional
    public void deactivateSettlementAccount(Long userId) {
        BankAccount primary = bankMapper.findPrimaryByUserId(userId);
        if (primary == null) {
            throw new BusinessException(BANK_PRIMARY_ACCOUNT_NOT_FOUND);
        }
        bankMapper.deactivateSettlementAccountsByUserId(userId);
    }

    public List<BankAccountSummaryResponse> getAccounts(Long userId) {
        BankAccount primary = bankMapper.findPrimaryByUserId(userId);
        List<BankCandidateDto> candidates = bankCandidateStore.findAll(userId);
        Set<String> selected = bankSelectedStore.findAll(userId);

        Map<String, BankAccountSummaryResponse> result = new LinkedHashMap<>();
        if (primary != null) {
            result.put(primary.getFintechUseNum(), BankAccountSummaryResponse.from(primary));
        }

        if (candidates != null) {
            for (BankCandidateDto candidate : candidates) {
                if (selected.contains(candidate.getFintechUseNum())) {
                    result.putIfAbsent(
                            candidate.getFintechUseNum(),
                            BankAccountSummaryResponse.fromCandidate(candidate)
                    );
                }
            }
        }

        return new ArrayList<>(result.values());
    }

    @Transactional(readOnly = true)
    public PrimaryBankAccountResponse getPrimaryAccount(Long userId) {
        BankAccount primary = bankMapper.findPrimaryByUserId(userId);
        if (primary == null) {
            throw new BusinessException(BANK_PRIMARY_ACCOUNT_NOT_FOUND);
        }
        return PrimaryBankAccountResponse.from(primary);
    }

    private List<BankCandidateDto> recoverCandidates(Long userId) {
        Optional<BankAuthTokenDto> tokenOpt = bankAuthTokenStore.find(userId);
        if (tokenOpt.isEmpty()) {
            return null;
        }

        BankAuthTokenDto stored = tokenOpt.get();
        KftcTokenResponse syntheticToken = new KftcTokenResponse(
                stored.getAccessToken(),
                stored.getRefreshToken(),
                stored.getUserSeqNo(),
                null,
                null,
                null
        );

        KftcUserInfoResponse userInfo = kftcApiClient.requestUserInfo(syntheticToken);
        if (userInfo.getResList() == null || userInfo.getResList().isEmpty()) {
            return List.of();
        }

        List<BankCandidateDto> recovered = userInfo.getResList().stream()
                .map(dto -> BankCandidateDto.builder()
                        .fintechUseNum(dto.getFintechUseNum())
                        .bankTranId(dto.getBankTranId())
                        .bankName(dto.getBankName())
                        .accountAlias(dto.getAccountAlias())
                        .accountNumMasked(dto.getAccountNumMasked())
                        .accessToken(stored.getAccessToken())
                        .refreshToken(stored.getRefreshToken())
                        .build())
                .toList();

        return sanitizeCandidates(recovered);
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

    private List<BankCandidateDto> sanitizeCandidates(List<BankCandidateDto> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        Map<String, BankCandidateDto> dedup = new LinkedHashMap<>();
        for (BankCandidateDto candidate : raw) {
            if (candidate.getFintechUseNum() == null || candidate.getFintechUseNum().isBlank()) {
                continue;
            }

            String key = nz(candidate.getBankName()) + "|" + nz(candidate.getAccountNumMasked());
            if (key.equals("|")) {
                key = candidate.getFintechUseNum();
            }

            dedup.putIfAbsent(key, candidate);
        }

        return new ArrayList<>(dedup.values());
    }

    private String nz(String value) {
        return value == null ? "" : value.trim();
    }
}
