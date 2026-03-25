<<<<<<< HEAD
package pbl2.sub119.backend.subproduct.dto;
=======
package pbl2.submate.backend.subproduct.dto;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
<<<<<<< HEAD
import pbl2.sub119.backend.subproduct.entity.SubProduct;
=======
import pbl2.submate.backend.subproduct.entity.SubProduct;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubProductResponse {
    private String id;
    private String serviceName;
    private String description;
    private String thumbnailUrl;
    private String operationType;
    private Integer maxMemberCount;
    private Long basePrice;
    private Long pricePerMember;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubProductResponse from(SubProduct entity) {
        return SubProductResponse.builder()
                .id(entity.getId())
                .serviceName(entity.getServiceName())
                .description(entity.getDescription())
                .thumbnailUrl(entity.getThumbnailUrl())
                .operationType(entity.getOperationType())
                .maxMemberCount(entity.getMaxMemberCount())
                .basePrice(entity.getBasePrice())
                .pricePerMember(entity.getPricePerMember())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
