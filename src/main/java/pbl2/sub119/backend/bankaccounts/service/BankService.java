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

    /**
     * KFTC 콜백 처리: Redis 후보 캐시(10분) + 인증 토큰 캐시(45분)만 저장한다.
     * DB upsert 없음 — callback 시 전체 계좌 인젝션 방지.
     * 인증 토큰은 후보 TTL 만료 후 복구 경로로 사용된다.
     */
    public void registerAccount(Long userId, String code) {
        log.info("KFTC account registration started. userId={}", userId);

        KftcTokenResponse tokenResponse = kftcApiClient.requestToken(code);
        KftcUserInfoResponse userInfo = kftcApiClient.requestUserInfo(tokenResponse);

        // 새 인증 스냅샷 저장 전 기존 캐시를 제거해 덮어쓰기를 보장한다
        bankCandidateStore.remove(userId);
        bankAuthTokenStore.remove(userId);
        bankSelectedStore.remove(userId);

        // 후보 TTL(10분) 만료 후에도 settlement 복구를 위해 별도 저장(45분)
        bankAuthTokenStore.save(userId,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getUserSeqNo());

        if (userInfo.getResList() == null || userInfo.getResList().isEmpty()) {
            log.warn("KFTC account list is empty. userId={}", userId);
            bankCandidateStore.save(userId, List.of());
            // null(miss)과 구분하기 위해 명시적 빈 스냅샷 저장
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

        //filter
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

        Optional<BankCandidateDto> candidateOpt = bankCandidateStore.findByFintechUseNum(userId, request.getFintechUseNum());

        if (candidateOpt.isPresent()) {
            // 경로 A: Redis 후보 보유 → 해당 1건만 DB row 보장
            ensureKftcRowExists(userId, candidateOpt.get());
        } else {
            // 경로 B/C: Redis 후보 miss
            // recoverCandidates: auth token 없으면 null, KFTC 장애면 BusinessException 전파
            List<BankCandidateDto> recovered = recoverCandidates(userId);

            if (recovered != null) {
                // 경로 B: auth token 보유 + KFTC 재조회 성공
                Optional<BankCandidateDto> recoveredOpt = recovered.stream()
                        .filter(c -> request.getFintechUseNum().equals(c.getFintechUseNum()))
                        .findFirst();
                if (recoveredOpt.isPresent()) {
                    ensureKftcRowExists(userId, recoveredOpt.get());
                    bankCandidateStore.save(userId, recovered); // 재캐시
                    ensureKftcRowExists(userId, recoveredOpt.get());
                } else {
                    // KFTC에 해당 계좌 없음: 링크 해제 상태
                    throw new BusinessException(BANK_AUTH_EXPIRED);
                }
            } else {
                // 경로 C: auth token도 만료 → 재인증 요구
                // getAccounts가 miss 시 빈 목록을 반환하는 정책과 일관성 유지
                throw new BusinessException(BANK_AUTH_EXPIRED);
            }
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

    /**
     * 계좌 목록 조회 (표시 전용 — 엄격):
     * - 활성 정산계좌(primary) 항상 포함
     * - 이번 인증 후보 중 POST /settlement 완료된 것만 포함 (화이트리스트 필터)
     * - 후보 전체 노출 없음 / DB fallback 없음 (레거시 미노출 보장)
     */
    public List<BankAccountSummaryResponse> getAccounts(Long userId) {
        BankAccount primary = bankMapper.findPrimaryByUserId(userId);
        List<BankCandidateDto> candidates = bankCandidateStore.findAll(userId);
        Set<String> selected = bankSelectedStore.findAll(userId);

        // fintechUseNum 기준 중복 제거, primary 우선
        Map<String, BankAccountSummaryResponse> result = new LinkedHashMap<>();

        if (primary != null) {
            result.put(primary.getFintechUseNum(), BankAccountSummaryResponse.from(primary));
        }

        if (candidates != null) {
            // 후보 전체 노출 금지: selected 화이트리스트만 통과
            for (BankCandidateDto c : candidates) {
                if (selected.contains(c.getFintechUseNum())) {
                    result.putIfAbsent(c.getFintechUseNum(), BankAccountSummaryResponse.fromCandidate(c));
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

    // ── private helpers ──────────────────────────────────────────────────────

    /**
     * auth token Redis 캐시로 KFTC userInfo를 재조회해 후보 목록을 재구성한다.
     * <p>
     * null 반환: auth token 미보유(Redis miss) — caller가 DB fallback 처리.
     * List 반환: KFTC 재조회 성공 (빈 리스트 포함).
     * 예외 전파: KFTC API 장애 시 {@code BANK_ACCOUNT_CONNECT_REQUEST_FAILED} —
     *            settlement은 전파, getAccounts는 흡수 후 DB fallback.
     */
    private List<BankCandidateDto> recoverCandidates(Long userId) {
        Optional<BankAuthTokenDto> tokenOpt = bankAuthTokenStore.find(userId);
        if (tokenOpt.isEmpty()) {
            return null;
        }
        // auth token 보유 → KFTC 재조회. BusinessException(BANK007)은 그대로 전파.
        BankAuthTokenDto stored = tokenOpt.get();
        KftcTokenResponse syntheticToken = new KftcTokenResponse(
                stored.getAccessToken(), stored.getRefreshToken(), stored.getUserSeqNo(),
                null, null, null);

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

    /**
     * fintechUseNum 기준으로 DB row가 있으면 KFTC 메타만 갱신, 없으면 신규 삽입.
     * settlement 메타(account_type, is_primary, verification_*)는 updateBankAccount SQL이 건드리지 않는다.
     */
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
        if (raw == null || raw.isEmpty()) return List.of();

        // 순서 유지 + 중복 제거
        Map<String, BankCandidateDto> dedup = new LinkedHashMap<>();

        for (BankCandidateDto c : raw) {
            if (c.getFintechUseNum() == null || c.getFintechUseNum().isBlank()) continue;

            // 같은 은행 + 같은 마스킹 계좌는 1건만 노출
            String key = nz(c.getBankName()) + "|" + nz(c.getAccountNumMasked());
            if (key.equals("|")) {
                key = c.getFintechUseNum();
            }

            // 키가 비정상이면 fintech 번호로 대체
            if (key.equals("|")) key = c.getFintechUseNum();

            dedup.putIfAbsent(key, c);
        }

        return new ArrayList<>(dedup.values());
    }

    private String nz(String v) {
        return v == null ? "" : v.trim();
    }
}
