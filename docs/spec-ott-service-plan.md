# OTT 서비스 플랜 정보 — 상품 도메인 연동 명세

## 배경

동시접속 제어 기능 구현 과정에서 OTT 서비스별 동시 접속 허용 수와 해상도 정보가 필요합니다.  
현재 `GET /api/v1/ott-service-plans`는 정적 하드코딩으로 동작하고 있습니다.  
`SubProduct`에 아래 두 필드가 추가되면 실제 데이터 기반 응답으로 교체합니다.

---

## 요청 작업 (상품 도메인)

### 1. SubProduct 엔티티 필드 추가

```java
// SubProduct.java
private Integer concurrentLimit;  // 동시 접속 허용 기기 수 (예: 2, 4)
private String  resolution;       // 해상도 (예: "FHD", "4K")
```

### 2. schema.sql DDL 추가

```sql
ALTER TABLE sub_product ADD COLUMN IF NOT EXISTS concurrent_limit INT NULL;
ALTER TABLE sub_product ADD COLUMN IF NOT EXISTS resolution        VARCHAR(20) NULL;
```

### 3. SubProductRequest / SubProductUpdateRequest 필드 추가

```java
private Integer concurrentLimit;
private String  resolution;
```

### 4. SubProductMapper.xml resultMap 및 INSERT/UPDATE 쿼리 반영

기존 resultMap에 아래 매핑 추가:

```xml
<result property="concurrentLimit" column="concurrent_limit"/>
<result property="resolution"      column="resolution"/>
```

---

## 연동 후 처리 (동시접속 도메인)

위 작업이 완료되면 아래 파일을 수정합니다.

**`OttServicePlanController.java`**  
`getPlans()` 메서드를 `subProductMapper.findAll()` 기반으로 교체합니다.

```java
// 교체 예시
@GetMapping
public ResponseEntity<List<OttServicePlanResponse>> getPlans() {
    return ResponseEntity.ok(
        subProductMapper.findAll().stream()
            .map(p -> new OttServicePlanResponse(
                p.getCategory().name(),
                p.getServiceName(),
                p.getConcurrentLimit(),
                p.getResolution()
            ))
            .toList()
    );
}
```

---

## 현재 하드코딩 데이터 (참고)

| 서비스 | 플랜 | 동시접속 | 해상도 |
|--------|------|----------|--------|
| NETFLIX | 스탠다드 | 2 | FHD |
| NETFLIX | 프리미엄 | 4 | 4K |
| TVING | 베이직 | 1 | HD |
| TVING | 스탠다드 | 2 | FHD |
| TVING | 프리미엄 | 4 | FHD |
| WATCHA | 베이직 | 1 | FHD |
| WATCHA | 프리미엄 | 4 | FHD |
| DISNEY_PLUS | 스탠다드 | 2 | FHD |
| DISNEY_PLUS | 프리미엄 | 4 | 4K |
| WAVVE | 베이직 | 1 | HD |
| WAVVE | 스탠다드 | 2 | FHD |
| WAVVE | 프리미엄 | 4 | FHD |
