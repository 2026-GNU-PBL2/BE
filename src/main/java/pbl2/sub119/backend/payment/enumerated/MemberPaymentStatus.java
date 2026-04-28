package pbl2.sub119.backend.payment.enumerated;

public enum MemberPaymentStatus {
    PAYMENT_PENDING,
    PROCESSING,
    PAID,
    FAILED,
    CANCELLED;

    public boolean isTerminal() {
        return this == PAID || this == FAILED || this == CANCELLED;
    }
}