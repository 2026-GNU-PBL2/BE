package pbl2.sub119.backend.toss.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.toss.entity.BillingKeyEntity;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BillingKeyMapper {
    void insert(BillingKeyEntity billingKey);
    Optional<BillingKeyEntity> findByUserId(@Param("userId") Long userId);
    Optional<BillingKeyEntity> findAnyByUserId(@Param("userId") Long userId);
    List<BillingKeyEntity> findActiveByPartyId(@Param("partyId") Long partyId);
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    void updateBillingKeyInfo(
            @Param("id") Long id,
            @Param("billingKey") String billingKey,
            @Param("cardCompany") String cardCompany,
            @Param("maskedCardNumber") String maskedCardNumber
    );
}
