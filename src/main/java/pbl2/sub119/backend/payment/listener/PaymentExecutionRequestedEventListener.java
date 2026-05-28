package pbl2.sub119.backend.payment.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.payment.event.PaymentExecutionRequestedEvent;
import pbl2.sub119.backend.payment.service.AutoPaymentService;

/**
 * 결제 실행 요청 이벤트 리스너.
 *
 * <h2>성능 개선: 동기 → @Async 비동기 실행</h2>
 * <pre>
 * 변경 전: retry() 스레드 → afterCommit() → publishEvent() → handle() 동기 실행
 *          → execute() 전체(PG I/O ~600ms) 완료까지 HTTP 스레드 블로킹
 *          → Tomcat 스레드 고갈, p99 급등
 *
 * 변경 후: retry() 스레드 → afterCommit() → publishEvent()
 *          → paymentExecutor 큐 투입 후 즉시 리턴 (~3ms)
 *          → execute()는 paymentExecutor 스레드풀에서 독립 실행
 * </pre>
 *
 * <h2>이벤트 유실 및 복구 경로</h2>
 * JVM 크래시 시 큐에 투입된 작업이 유실될 수 있다 (변경 전도 afterCommit 이후 크래시 시 동일).
 * 완전한 내구성이 필요하면 Outbox 패턴 도입이 필요하다.
 * 현재 복구 경로: party_cycle.status = PAYMENT_PENDING 잔존 레코드를
 * {@link pbl2.sub119.backend.payment.service.DeadlinePaymentService}가 GRACE_HOURS(24h) 후 FAILED 처리.
 * 이후 어드민이 /retry 재호출로 수동 복구 가능.
 *
 * <h2>중복 실행 방지</h2>
 * execute() Phase1의 claimCycle CAS(PAYMENT_PENDING→PROCESSING)가
 * 동일 partyCycleId에 대해 단 한 번만 성공하므로, @Async로 인한 중복 처리는 없다.
 *
 * <h2>CallerRunsPolicy 발동 조건</h2>
 * paymentExecutor: corePool=40, maxPool=80, queue=200.
 * 동시 작업이 280(= maxPool + queue)을 초과하면 publishEvent() 호출 스레드(Tomcat)에서
 * execute()를 동기 실행한다 → 일시적 p99 상승 가능.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExecutionRequestedEventListener {

    private final AutoPaymentService autoPaymentService;

    @Async("paymentExecutor")
    @EventListener
    public void handle(PaymentExecutionRequestedEvent event) {
        log.info("자동결제 실행 시작 [비동기]. partyId={}, partyCycleId={}",
                event.partyId(), event.partyCycleId());

        autoPaymentService.execute(event.partyId(), event.partyCycleId());
    }
}
