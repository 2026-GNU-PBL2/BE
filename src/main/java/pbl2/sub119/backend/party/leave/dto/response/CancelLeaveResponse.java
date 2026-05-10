package pbl2.sub119.backend.party.leave.dto.response;

public record CancelLeaveResponse(
        LeaveCancelResult result,
        String message
) {
    public static CancelLeaveResponse cancelled() {
        return new CancelLeaveResponse(
                LeaveCancelResult.CANCELLED,
                "탈퇴 예약이 취소되었습니다."
        );
    }

    public static CancelLeaveResponse forcedLeft() {
        return new CancelLeaveResponse(
                LeaveCancelResult.FORCED_LEFT,
                "입장 대기자가 있어 탈퇴가 확정되었습니다. 자동 재매칭을 시작합니다."
        );
    }
}
