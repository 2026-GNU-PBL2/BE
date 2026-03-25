package pbl2.sub119.backend.subproduct.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
<<<<<<< HEAD
import pbl2.sub119.backend.common.enumerated.UserRole;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.AuthException;
import pbl2.sub119.backend.subproduct.controller.docs.SubProductAdminDocs;
import pbl2.sub119.backend.subproduct.dto.SubProductRequest;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.dto.SubProductUpdateRequest;
import pbl2.sub119.backend.subproduct.service.SubProductService;
=======
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.AuthException;
import pbl2.submate.backend.subproduct.controller.docs.SubProductAdminDocs;
import pbl2.submate.backend.subproduct.dto.SubProductRequest;
import pbl2.submate.backend.subproduct.dto.SubProductResponse;
import pbl2.submate.backend.subproduct.dto.SubProductUpdateRequest;
import pbl2.submate.backend.subproduct.service.SubProductService;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class SubProductAdminController implements SubProductAdminDocs {

    private final SubProductService subProductService;

    @GetMapping
    public ResponseEntity<List<SubProductResponse>> getProducts(
            @Auth final Accessor accessor) {
        validateAdmin(accessor);
        return ResponseEntity.ok(subProductService.getProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubProductResponse> getProduct(
            @Auth final Accessor accessor,
            @PathVariable String id) {
        validateAdmin(accessor);
        return ResponseEntity.ok(subProductService.getProduct(id));
    }

    /**
     * POST /api/v1/admin/products
     * {
     *   "serviceName"    : "넷플릭스 프리미엄",
     *   "description"    : "4명이 함께 쓰는 넷플릭스",
     *   "thumbnailUrl"   : "https://cdn.submate.io/netflix.png",
     *   "operationType"  : "ACCOUNT_SHARE",
     *   "maxMemberCount" : 4,
     *   "basePrice"      : 17000,
     *   "pricePerMember" : 4500
     * }
     */
    @PostMapping
    public ResponseEntity<SubProductResponse> createProduct(
            @Auth final Accessor accessor,
            @Valid @RequestBody SubProductRequest request) {
        validateAdmin(accessor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subProductService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubProductResponse> updateProduct(
            @Auth final Accessor accessor,
            @PathVariable String id,
            @Valid @RequestBody SubProductUpdateRequest request) {
        validateAdmin(accessor);
        return ResponseEntity.ok(subProductService.updateProduct(id, request));
    }

    private void validateAdmin(Accessor accessor) {
        if (!UserRole.ADMIN.equals(accessor.getRole())) {
            throw new AuthException(ErrorCode.AUTH_FORBIDDEN);
        }
    }
}