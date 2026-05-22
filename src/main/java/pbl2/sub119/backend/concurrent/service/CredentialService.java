package pbl2.sub119.backend.concurrent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.util.CryptoUtil;
import pbl2.sub119.backend.concurrent.dto.response.CredentialResponse;
import pbl2.sub119.backend.concurrent.exception.ConcurrentException;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.notification.service.WebMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final PartyProvisionMapper partyProvisionMapper;
    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final SubProductMapper subProductMapper;
    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService smsTemplate;
    private final WebMessageTemplateService webTemplate;
    private final CryptoUtil cryptoUtil;

    // 파티원이 이용 정보(계정 정보)를 조회
    public CredentialResponse getCredential(final Long partyId, final Long userId) {
        final PartyMember member = partyMemberMapper.findByPartyIdAndUserId(partyId, userId);
        if (member == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }

        final PartyProvision provision = partyProvisionMapper.findByPartyId(partyId);
        if (provision == null) {
            throw new ConcurrentException(ErrorCode.NOT_FOUND);
        }

        final String password = provision.getSharedAccountPasswordEncrypted() != null
                ? cryptoUtil.decrypt(provision.getSharedAccountPasswordEncrypted())
                : null;

        return CredentialResponse.builder()
                .sharedAccountEmail(provision.getSharedAccountEmail())
                .sharedAccountPassword(password)
                .build();
    }

    // 파티장이 비밀번호를 변경하고 파티원 전체에 이용 정보 재등록 알림 발송
    @Transactional
    public void notifyCredentialsUpdated(final Long partyId, final Long hostUserId) {
        final PartyMember host = partyMemberMapper.findByPartyIdAndUserId(partyId, hostUserId);
        if (host == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }

        final String productName = subProductMapper.findById(
                partyMapper.findById(partyId).getProductId()
        ).map(p -> p.getServiceName()).orElse("서비스");

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(partyId);
        for (final PartyMember member : members) {
            if (!member.getUserId().equals(hostUserId)) {
                notificationCommandService.notifyWithWebContent(
                        member.getUserId(), partyId,
                        NotificationType.CREDENTIALS_UPDATED,
                        smsTemplate.getTitle(NotificationType.CREDENTIALS_UPDATED),
                        smsTemplate.credentialsUpdated(productName),
                        webTemplate.credentialsUpdated(productName)
                );
            }
        }
    }
}
