package pbl2.sub119.backend.bankaccounts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.bankaccounts.dto.BankAuthTokenDto;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankAuthTokenStore {

    private static final String KEY_PREFIX = "bank:auth:";
    // 후보 캐시(10분)보다 긴 TTL로 miss 복구 경로를 보장한다
    private static final Duration TTL = Duration.ofMinutes(45);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public boolean save(Long userId, String accessToken, String refreshToken, String userSeqNo) {
        try {
            BankAuthTokenDto dto = BankAuthTokenDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userSeqNo(userSeqNo)
                    .build();
            redisTemplate.opsForValue().set(KEY_PREFIX + userId, objectMapper.writeValueAsString(dto), TTL);
            return true;
        } catch (Exception e) {
            log.warn("Failed to save bank auth token to Redis. userId={}", userId, e);
            return false;
        }
    }

    public Optional<BankAuthTokenDto> find(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, BankAuthTokenDto.class));
        } catch (Exception e) {
            log.warn("Failed to read bank auth token from Redis. userId={}", userId, e);
            return Optional.empty();
        }
    }

    public void remove(Long userId) {
        try {
            redisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Failed to remove bank auth token from Redis. userId={}", userId, e);
        }
    }
}
