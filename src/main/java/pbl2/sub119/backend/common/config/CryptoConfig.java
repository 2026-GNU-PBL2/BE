package pbl2.sub119.backend.common.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pbl2.sub119.backend.common.util.CryptoUtil;

@Configuration
public class CryptoConfig {

    @Value("${crypto.aes-secret-key}")
    private String secret;

    @Bean
    public CryptoUtil cryptoUtil() {
        byte[] keyBytes = secret.getBytes();

        byte[] key = new byte[32];
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));

        SecretKey secretKey = new SecretKeySpec(key, "AES");
        return new CryptoUtil(secretKey);
    }
}