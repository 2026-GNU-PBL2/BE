package pbl2.sub119.backend.concurrent.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.concurrent.dto.response.ViolationHistoryResponse;
import pbl2.sub119.backend.concurrent.mapper.UserViolationRecordMapper;

@Service
@RequiredArgsConstructor
public class ViolationQueryService {

    private final UserViolationRecordMapper violationRecordMapper;

    public List<ViolationHistoryResponse> getByUserId(final Long userId) {
        return ViolationHistoryResponse.fromList(violationRecordMapper.findByUserId(userId));
    }

    public List<ViolationHistoryResponse> getByPartyId(final Long partyId) {
        return ViolationHistoryResponse.fromList(violationRecordMapper.findByPartyId(partyId));
    }
}
