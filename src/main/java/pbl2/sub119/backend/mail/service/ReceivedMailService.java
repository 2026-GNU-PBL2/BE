package pbl2.sub119.backend.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.mail.dto.response.ReceivedMailDetailResponse;
import pbl2.sub119.backend.mail.dto.response.ReceivedMailResponse;
import pbl2.sub119.backend.mail.entity.ReceivedMail;
import pbl2.sub119.backend.mail.mapper.ReceivedMailMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceivedMailService {

    private final ReceivedMailMapper receivedMailMapper;

    public List<ReceivedMailResponse> getMyMails(final Accessor accessor) {
        return receivedMailMapper.findByUserId(accessor.getUserId())
                .stream()
                .map(ReceivedMailResponse::from)
                .toList();
    }

    public ReceivedMailDetailResponse getMail(final Accessor accessor, final Long mailId) {
        final ReceivedMail mail = receivedMailMapper.findByIdAndUserId(mailId, accessor.getUserId());

        if (mail == null) {
            throw new BusinessException(ErrorCode.RECEIVED_MAIL_NOT_FOUND);
        }

        return ReceivedMailDetailResponse.from(mail);
    }
}
