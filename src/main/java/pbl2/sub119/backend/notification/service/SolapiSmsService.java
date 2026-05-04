package pbl2.sub119.backend.notification.service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.common.config.SolapiProperties;
import pbl2.sub119.backend.notification.entity.SmsSendLog;
import pbl2.sub119.backend.notification.enumerated.SmsSendStatus;
import pbl2.sub119.backend.notification.mapper.SmsSendLogMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolapiSmsService {

    private final SolapiProperties solapiProperties;
    private final SmsSendLogMapper smsSendLogMapper;

    private DefaultMessageService messageService;

    @PostConstruct
    void init() {
        if (!solapiProperties.isEnabled()) {
            log.info("SOLAPI 비활성화 상태입니다.");
            return;
        }

        this.messageService = SolapiClient.INSTANCE.createInstance(
                solapiProperties.getApiKey(),
                solapiProperties.getApiSecret()
        );
    }

    public SmsSendStatus send(
            final Long userId,
            final String to,
            final String content,
            final Long notificationId
    ) {
        if (!solapiProperties.isEnabled() || messageService == null) {
            saveLog(notificationId, userId, maskPhoneNumber(to), content, SmsSendStatus.SKIPPED, "SOLAPI disabled");
            return SmsSendStatus.SKIPPED;
        }

        try {
            Message message = new Message();
            message.setFrom(normalizePhoneNumber(solapiProperties.getSenderNumber()));
            message.setTo(normalizePhoneNumber(to));
            message.setText(content);

            MultipleDetailMessageSentResponse response = messageService.send(message);

            saveLog(notificationId, userId, maskPhoneNumber(to), content, SmsSendStatus.SUCCESS, null);
            log.info("SMS 발송 성공. userId={}, notificationId={}, response={}", userId, notificationId, response);

            return SmsSendStatus.SUCCESS;
        } catch (SolapiMessageNotReceivedException e) {
            saveLog(notificationId, userId, maskPhoneNumber(to), content, SmsSendStatus.FAILED, e.getMessage());
            log.error("SMS 접수 실패. userId={}, notificationId={}, failedMessages={}",
                    userId, notificationId, e.getFailedMessageList(), e);
            return SmsSendStatus.FAILED;
        } catch (SolapiUnknownException | SolapiEmptyResponseException e) {
            saveLog(notificationId, userId, maskPhoneNumber(to), content, SmsSendStatus.FAILED, e.getMessage());
            log.error("SOLAPI 응답 실패. userId={}, notificationId={}", userId, notificationId, e);
            return SmsSendStatus.FAILED;
        } catch (Exception e) {
            saveLog(notificationId, userId, maskPhoneNumber(to), content, SmsSendStatus.FAILED, e.getMessage());
            log.error("SMS 발송 실패. userId={}, notificationId={}", userId, notificationId, e);
            return SmsSendStatus.FAILED;
        }
    }

    private String maskPhoneNumber(final String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    private void saveLog(
            Long notificationId,
            Long userId,
            String phoneNumber,
            String content,
            SmsSendStatus status,
            String failReason
    ) {
        smsSendLogMapper.insert(SmsSendLog.builder()
                .notificationId(notificationId)
                .userId(userId)
                .phoneNumber(phoneNumber)
                .content(content)
                .status(status)
                .failReason(failReason)
                .createdAt(LocalDateTime.now())
                .build());
    }
}