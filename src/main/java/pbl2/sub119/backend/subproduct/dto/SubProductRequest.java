package pbl2.sub119.backend.subproduct.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;
import pbl2.sub119.backend.subproduct.enumerated.SubProductCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubProductRequest {

    @NotNull(message = "서비스명은 필수입니다.")
    private String serviceName;

    private String description;
    private String thumbnailUrl;

    @NotNull(message = "운영 방식은 필수입니다.")
    private OperationType operationType;

    @NotNull(message = "카테고리는 필수입니다.")
    private SubProductCategory category;

    @NotNull(message = "최대 인원은 필수입니다.")
    @Positive(message = "최대 인원은 1 이상이어야 합니다.")
    private Integer maxMemberCount;

    @NotNull(message = "전체 구독료는 필수입니다.")
    @Positive(message = "전체 구독료는 0보다 커야 합니다.")
    private Long basePrice;

    @NotNull(message = "1인당 결제 금액은 필수입니다.")
    @Positive(message = "1인당 결제 금액은 0보다 커야 합니다.")
    private Long pricePerMember;
}
