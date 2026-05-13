package pbl2.sub119.backend.bankaccounts.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.bankaccounts.dto.BankCandidateDto;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankCandidateStore {

    private static final String KEY_PREFIX = "bank:candidates:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(Long userId, List<BankCandidateDto> candidates) {
        try {
            String json = objectMapper.writeValueAsString(candidates);
            redisTemplate.opsForValue().set(KEY_PREFIX + userId, json, TTL);
        } catch (Exception e) {
            log.warn("Failed to save bank candidates to Redis. userId={}", userId, e);
        }
    }

    public List<BankCandidateDto> findAll(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to read bank candidates from Redis. userId={}", userId, e);
            return null;
        }
    }

    public Optional<BankCandidateDto> findByFintechUseNum(Long userId, String fintechUseNum) {
        List<BankCandidateDto> all = findAll(userId);
        if (all == null) {
            return Optional.empty();
        }
        return all.stream()
                .filter(c -> fintechUseNum.equals(c.getFintechUseNum()))
                .findFirst();
    }

    public void remove(Long userId) {
        try {
            redisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Failed to remove bank candidates from Redis. userId={}", userId, e);
        }
    }
}
