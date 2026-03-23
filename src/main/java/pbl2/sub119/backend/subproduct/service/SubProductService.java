package pbl2.sub119.backend.subproduct.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.common.exception.NotFoundException;
import pbl2.sub119.backend.subproduct.dto.SubProductRequest;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.entity.SubProduct;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubProductService {

    private final SubProductMapper subProductMapper;

    @Transactional(readOnly = true)
    public List<SubProductResponse> getProducts() {
        return subProductMapper.findAll()
                .stream()
                .map(SubProductResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubProductResponse getProduct(String id) {
        return subProductMapper.findById(id)
                .map(SubProductResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUB_PRODUCT_NOT_FOUND));
    }

    @Transactional
    public SubProductResponse createProduct(SubProductRequest request) {
        log.info("구독 상품 등록 시작. serviceName={}, operationType={}",
                request.getServiceName(), request.getOperationType());

        if (subProductMapper.existsByServiceName(request.getServiceName())) {
            throw new BusinessException(ErrorCode.SUB_PRODUCT_DUPLICATE_NAME);
        }

        SubProduct product = SubProduct.builder()
                .id(UUID.randomUUID().toString())
                .serviceName(request.getServiceName())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .operationType(request.getOperationType())
                .maxMemberCount(request.getMaxMemberCount())
                .basePrice(request.getBasePrice())
                .pricePerMember(request.getPricePerMember())
                .status("ACTIVE")
                .build();

        subProductMapper.save(product);

        log.info("구독 상품 등록 완료. id={}, serviceName={}", product.getId(), product.getServiceName());
        return SubProductResponse.from(product);
    }

    @Transactional
    public SubProductResponse updateProduct(String id, SubProductRequest request) {
        log.info("구독 상품 수정 시작. id={}", id);

        SubProduct existing = subProductMapper.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUB_PRODUCT_NOT_FOUND));

        if (subProductMapper.existsByServiceNameExcludeId(request.getServiceName(), id)) {
            throw new BusinessException(ErrorCode.SUB_PRODUCT_DUPLICATE_NAME);
        }

        // operation_type은 등록 후 변경 불가 → 기존 값 유지
        SubProduct updated = SubProduct.builder()
                .id(id)
                .serviceName(request.getServiceName())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .operationType(existing.getOperationType())
                .maxMemberCount(request.getMaxMemberCount())
                .basePrice(request.getBasePrice())
                .pricePerMember(request.getPricePerMember())
                .build();

        subProductMapper.update(updated);

        log.info("구독 상품 수정 완료. id={}", id);
        return subProductMapper.findById(id)
                .map(SubProductResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUB_PRODUCT_NOT_FOUND));
    }
}