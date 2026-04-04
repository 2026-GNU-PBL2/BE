package pbl2.sub119.backend.common.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pbl2.sub119.backend.common.util.CryptoUtil;
import java.nio.charset.StandardCharsets;

@Configuration
public class CryptoConfig {

    @Value("${crypto.aes-secret-key}")
    private String secret;

    @Bean
    public CryptoUtil cryptoUtil() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "AES 키는 정확히 32바이트여야 합니다. 현재: " + keyBytes.length + "바이트"
            );
        }

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        return new CryptoUtil(secretKey);
    }
}