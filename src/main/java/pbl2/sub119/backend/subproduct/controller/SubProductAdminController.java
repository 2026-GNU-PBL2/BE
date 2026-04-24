package pbl2.sub119.backend.subproduct.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.subproduct.controller.docs.SubProductAdminDocs;
import pbl2.sub119.backend.subproduct.dto.SubProductRequest;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.dto.SubProductUpdateRequest;
import pbl2.sub119.backend.subproduct.service.SubProductService;

import java.util.List;

@AdminOnly
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class SubProductAdminController implements SubProductAdminDocs {

    private final SubProductService subProductService;

    @GetMapping
    public ResponseEntity<List<SubProductResponse>> getProducts(
            @Auth final Accessor accessor) {
        return ResponseEntity.ok(subProductService.getProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubProductResponse> getProduct(
            @Auth final Accessor accessor,
            @PathVariable String id) {
        return ResponseEntity.ok(subProductService.getProduct(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubProductResponse> createProduct(
            @Auth final Accessor accessor,
            @RequestPart("data") @Valid SubProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subProductService.createProduct(request, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubProductResponse> updateProduct(
            @Auth final Accessor accessor,
            @PathVariable String id,
            @RequestPart("data") @Valid SubProductUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(subProductService.updateProduct(id, request, image));
    }
}
