package pbl2.sub119.backend.admin.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.WithdrawRequestStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.pointWallet.mapper.PointWalletMapper;
import pbl2.sub119.backend.settlement.dto.response.WithdrawRequestResponse;
import pbl2.sub119.backend.settlement.entity.PointWithdrawRequest;
import pbl2.sub119.backend.settlement.mapper.PointWithdrawRequestMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSettlementWithdrawService {

    private final PointWithdrawRequestMapper pointWithdrawRequestMapper;
    private final PointWalletMapper pointWalletMapper;

    @Transactional(readOnly = true)
    public List<WithdrawRequestResponse> getWithdrawRequests(WithdrawRequestStatus status, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int normalizedPage = page <= 1 ? 0 : page - 1;
        int offset = (int) Math.min((long) normalizedPage * safeSize, Integer.MAX_VALUE);
        return pointWithdrawRequestMapper.findByStatus(status, safeSize, offset)
                .stream()
                .map(WithdrawRequestResponse::from)
                .toList();
    }

    @Transactional
    public void completeWithdrawRequest(Long requestId, Long adminId, String externalTxId) {
        PointWithdrawRequest request = getRequestOrThrow(requestId);

        String cleanedExternalTxId = (externalTxId != null && !externalTxId.isBlank())
                ? externalTxId.trim()
                : null;

        int updated = pointWithdrawRequestMapper.updateStatusIfRequested(
                requestId,
                WithdrawRequestStatus.COMPLETED,
                adminId,
                null,
                cleanedExternalTxId
        );

        if (updated == 0) {
            throw new BusinessException(ErrorCode.SETTLEMENT_WITHDRAW_INVALID_STATUS);
        }

        log.info("환급 완료 처리. requestId={}, adminId={}, internalPayoutRef={}, externalTxId={}",
                requestId, adminId, request.getInternalPayoutRef(), cleanedExternalTxId);
    }

    @Transactional
    public void rejectWithdrawRequest(Long requestId, Long adminId, String reason) {
        PointWithdrawRequest request = getRequestOrThrow(requestId);

        int updated = pointWithdrawRequestMapper.updateStatusIfRequested(
                requestId,
                WithdrawRequestStatus.REJECTED,
                adminId,
                reason,
                null
        );

        if (updated == 0) {
            throw new BusinessException(ErrorCode.SETTLEMENT_WITHDRAW_INVALID_STATUS);
        }

        // REJECTED 시 차감했던 금액 복구
        pointWalletMapper.increaseBalance(request.getUserId(), request.getAmount());

        log.info("환급 거절 처리 및 포인트 복구. requestId={}, adminId={}, userId={}, amount={}",
                requestId, adminId, request.getUserId(), request.getAmount());
    }

    private PointWithdrawRequest getRequestOrThrow(Long requestId) {
        PointWithdrawRequest request = pointWithdrawRequestMapper.findById(requestId);
        if (request == null) {
            throw new BusinessException(ErrorCode.SETTLEMENT_WITHDRAW_REQUEST_NOT_FOUND);
        }
        return request;
    }
}
