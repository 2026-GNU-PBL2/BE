package pbl2.sub119.backend.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 결제 실행 비동기 설정.
 *
 * <p>성능 개선 목적:
 * PaymentExecutionRequestedEvent 리스너를 비동기로 전환하여
 * retry API 요청 스레드가 AutoPaymentService.execute() 완료를 기다리지 않도록 한다.
 * 이를 통해 HTTP 응답을 즉시 반환하고 Tomcat 스레드 압박을 완화한다.
 *
 * <p>스레드 풀 설계 근거:
 * - PG 호출은 I/O bound (stub: 120ms×N, 실 PG: 200~500ms+)
 * - 고부하 시 약 121 retry/s → 동시 execute 수 ≈ 121 × (5ms Phase1+5ms Phase3) / 1000 ≈ 2
 *   (3-Phase 분리 후 접속 보유 시간이 ~10ms이므로 DB 병목 해소)
 * - PG I/O 동시 수 ≈ 121 × 0.6s = ~73 → corePoolSize 40 설정
 * - RejectedExecutionHandler: CallerRunsPolicy → 큐 포화 시 호출 스레드(Tomcat)에서 동기 실행
 *   (degraded 모드로 동작, OOM/무한 큐 방지)
 */
@Slf4j
@Configuration
@EnableAsync
public class PaymentAsyncConfig implements AsyncConfigurer {

    /**
     * 결제 실행 전용 스레드풀.
     * 이름: "payment-exec-N" (모니터링/덤프에서 식별 가능)
     */
    @Bean("paymentExecutor")
    public Executor paymentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(80);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("payment-exec-");
        // 큐 포화 시 CallerRuns: 무한 큐/거절 대신 호출 스레드에서 처리 (서킷 브레이커 역할)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 비동기 메서드에서 발생한 미처리 예외 로깅.
     * execute()에서 예외 발생 시 HTTP 응답은 이미 204로 반환된 상태이므로
     * 로그로 기록 후 마감 처리기(DeadlinePaymentService)에 복구를 위임한다.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                log.error("[비동기 결제 실행 미처리 예외] method={}, params={}", method.getName(), params, ex);
            }
        };
    }
}
