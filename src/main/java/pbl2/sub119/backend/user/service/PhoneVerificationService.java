package pbl2.sub119.backend.user.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.notification.service.SolapiSmsService;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final String OTP_PREFIX = "otp:";
    private static final String VERIFIED_PREFIX = "otp:verified:";
    private static final long OTP_TTL_MINUTES = 3;
    private static final long VERIFIED_TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;
    private final SolapiSmsService solapiSmsService;

    public void sendOtp(final Long userId, final String phoneNumber) {
        final String code = generateCode();
        redisTemplate.opsForValue().set(
                OTP_PREFIX + phoneNumber,
                code,
                OTP_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        solapiSmsService.sendOtp(userId, phoneNumber, "[서브메이트] 인증번호: " + code);
    }

    public void confirm(final String phoneNumber, final String inputCode) {
        final String storedCode = redisTemplate.opsForValue().get(OTP_PREFIX + phoneNumber);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.PHONE_OTP_NOT_FOUND);
        }
        if (!storedCode.equals(inputCode)) {
            throw new BusinessException(ErrorCode.PHONE_OTP_MISMATCH);
        }

        redisTemplate.delete(OTP_PREFIX + phoneNumber);
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + phoneNumber,
                "true",
                VERIFIED_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public boolean isVerified(final String phoneNumber) {
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(VERIFIED_PREFIX + phoneNumber));
    }

    public void consumeVerification(final String phoneNumber) {
        redisTemplate.delete(VERIFIED_PREFIX + phoneNumber);
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
