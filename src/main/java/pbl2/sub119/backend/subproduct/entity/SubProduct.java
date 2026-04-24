package pbl2.sub119.backend.subproduct.entity;

import lombok.*;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;
import pbl2.sub119.backend.subproduct.enumerated.SubProductCategory;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SubProduct {

    private String id;
    private String serviceName;
    private String description;
    private String thumbnailUrl;

    private OperationType operationType;
    private SubProductCategory category;

    private Integer maxMemberCount;
    private Long basePrice;
    private Long pricePerMember;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
