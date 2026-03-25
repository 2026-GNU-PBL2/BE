
package pbl2.sub119.backend.subproduct.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

import pbl2.sub119.backend.subproduct.controller.docs.SubProductDocs;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.service.SubProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class SubProductController implements SubProductDocs {

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
}