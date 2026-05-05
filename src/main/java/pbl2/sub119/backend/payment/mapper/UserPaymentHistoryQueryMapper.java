package pbl2.sub119.backend.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.payment.dto.response.PaymentHistoryItem;

import java.util.List;

@Mapper
public interface UserPaymentHistoryQueryMapper {

    List<PaymentHistoryItem> findByUserId(
            @Param("userId") Long userId,
            @Param("size") int size,
            @Param("offset") int offset
    );
}
