package pbl2.sub119.backend.payment.policy;

public final class FeePolicy {

    private FeePolicy() {}

    // 파티원 고정 수수료
    public static final long MEMBER_FEE = 990L;

    // 파티장 수수료 = 고정 수수료(990) - 파티장 할인(500)
    public static final long HOST_FEE = 490L;

    // 파티장 귀책 시 최대 차감액
    public static final long HOST_MAX_PENALTY = 10_000L;
}
