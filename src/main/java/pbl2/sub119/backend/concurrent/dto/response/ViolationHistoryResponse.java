package pbl2.sub119.backend.concurrent.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import pbl2.sub119.backend.concurrent.entity.UserViolationRecord;
import pbl2.sub119.backend.concurrent.enumerated.ViolationType;

@Getter
@Builder
public class ViolationHistoryResponse {

    private Long recordId;
    private Long partyId;
    private ViolationType violationType;
    private BigDecimal weight;
    private LocalDateTime createdAt;

    public static ViolationHistoryResponse from(final UserViolationRecord record) {
        return ViolationHistoryResponse.builder()
                .recordId(record.getId())
                .partyId(record.getPartyId())
                .violationType(record.getViolationType())
                .weight(record.getWeight())
                .createdAt(record.getCreatedAt())
                .build();
    }

    public static List<ViolationHistoryResponse> fromList(final List<UserViolationRecord> records) {
        return records.stream().map(ViolationHistoryResponse::from).toList();
    }
}
