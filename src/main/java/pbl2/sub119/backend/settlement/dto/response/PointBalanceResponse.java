package pbl2.sub119.backend.settlement.dto.response;

public record PointBalanceResponse(
        Long balance
) {
    public static PointBalanceResponse of(Long balance) {
        return new PointBalanceResponse(balance == null ? 0L : balance);
    }
}
