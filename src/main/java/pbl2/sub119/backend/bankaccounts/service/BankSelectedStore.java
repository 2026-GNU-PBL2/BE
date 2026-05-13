package pbl2.sub119.backend.bankaccounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankSelectedStore {

    private static final String KEY_PREFIX = "bank:selected:";
    // 후보 TTL(10분)보다 충분히 길게 유지 — 정산 등록 이력 보존
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public void add(Long userId, String fintechUseNum) {
        String key = KEY_PREFIX + userId;
        try {
            redisTemplate.opsForSet().add(key, fintechUseNum);
            redisTemplate.expire(key, TTL);
        } catch (Exception e) {
            log.warn("Failed to add to bank selected store. userId={}, fintechUseNum={}", userId, fintechUseNum, e);
        }
    }

    public Set<String> findAll(Long userId) {
        try {
            Set<String> members = redisTemplate.opsForSet().members(KEY_PREFIX + userId);
            return members != null ? members : Set.of();
        } catch (Exception e) {
            log.warn("Failed to read bank selected store. userId={}", userId, e);
            return Set.of();
        }
    }

    public void remove(Long userId) {
        try {
            redisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Failed to remove bank selected store. userId={}", userId, e);
        }
    }
}
