<<<<<<< HEAD
package pbl2.sub119.backend.subproduct.controller;
=======
package pbl2.submate.backend.subproduct.controller;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
<<<<<<< HEAD
import pbl2.sub119.backend.subproduct.controller.docs.SubProductDocs;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.service.SubProductService;
=======
import pbl2.submate.backend.subproduct.controller.docs.SubProductDocs;
import pbl2.submate.backend.subproduct.dto.SubProductResponse;
import pbl2.submate.backend.subproduct.service.SubProductService;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)

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