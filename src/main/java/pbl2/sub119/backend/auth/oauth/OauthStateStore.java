package pbl2.sub119.backend.auth.oauth;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OauthStateStore {

    private static final String KEY_PREFIX = "sub119:oauth:state:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    public void save(String provider, String state) {
        redis.opsForValue().set(key(provider, state), "1", TTL);
    }

    public boolean consumeIfExists(String provider, String state) {
        String key = key(provider, state);
        Boolean exists = redis.hasKey(key);
        if (exists == null || !exists) return false;
        redis.delete(key);
        return true;
    }

    private String key(String provider, String state) {
        return KEY_PREFIX + provider.toLowerCase() + ":" + state;
    }
}
