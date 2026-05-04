package pbl2.sub119.backend.user.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.notification.enumerated.SmsSendStatus;
import pbl2.sub119.backend.notification.service.SolapiSmsService;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final String OTP_PREFIX = "otp:";
    private static final String VERIFIED_PREFIX = "otp:verified:";
    private static final String ATTEMPTS_PREFIX = "otp:attempts:";
    private static final long OTP_TTL_MINUTES = 3;
    private static final long VERIFIED_TTL_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redisTemplate;
    private final SolapiSmsService solapiSmsService;

    public void sendOtp(final Long userId, final String phoneNumber) {
        final String code = generateCode();

        // 재요청 시 이전 시도 횟수 초기화
        redisTemplate.delete(ATTEMPTS_PREFIX + phoneNumber);
        redisTemplate.opsForValue().set(
                OTP_PREFIX + phoneNumber,
                code,
                OTP_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        final SmsSendStatus status = solapiSmsService.sendOtp(
                userId, phoneNumber, "[서브메이트] 인증번호: " + code
        );

        if (status == SmsSendStatus.FAILED) {
            redisTemplate.delete(OTP_PREFIX + phoneNumber);
            throw new BusinessException(ErrorCode.PHONE_OTP_SEND_FAILED);
        }
    }

    public void confirm(final Long userId, final String phoneNumber, final String inputCode) {
        final String storedCode = redisTemplate.opsForValue().get(OTP_PREFIX + phoneNumber);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.PHONE_OTP_NOT_FOUND);
        }

        final String attemptsKey = ATTEMPTS_PREFIX + phoneNumber;
        final Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        redisTemplate.expire(attemptsKey, OTP_TTL_MINUTES, TimeUnit.MINUTES);

        if (attempts > MAX_ATTEMPTS) {
            redisTemplate.delete(OTP_PREFIX + phoneNumber);
            redisTemplate.delete(attemptsKey);
            throw new BusinessException(ErrorCode.PHONE_OTP_EXCEEDED);
        }

        if (!storedCode.equals(inputCode)) {
            throw new BusinessException(ErrorCode.PHONE_OTP_MISMATCH);
        }

        redisTemplate.delete(OTP_PREFIX + phoneNumber);
        redisTemplate.delete(attemptsKey);
        redisTemplate.opsForValue().set(
                verifiedKey(userId, phoneNumber),
                "true",
                VERIFIED_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public boolean isVerified(final Long userId, final String phoneNumber) {
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(verifiedKey(userId, phoneNumber)));
    }

    public void consumeVerification(final Long userId, final String phoneNumber) {
        redisTemplate.delete(verifiedKey(userId, phoneNumber));
    }

    private String verifiedKey(final Long userId, final String phoneNumber) {
        return VERIFIED_PREFIX + userId + ":" + phoneNumber;
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
