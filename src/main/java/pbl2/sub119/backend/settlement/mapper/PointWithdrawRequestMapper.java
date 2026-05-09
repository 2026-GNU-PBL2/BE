package pbl2.sub119.backend.settlement.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.common.enumerated.WithdrawRequestStatus;
import pbl2.sub119.backend.settlement.entity.PointWithdrawRequest;

import java.util.List;

@Mapper
public interface PointWithdrawRequestMapper {

    int insert(PointWithdrawRequest request);

    PointWithdrawRequest findById(@Param("id") Long id);

    List<PointWithdrawRequest> findByUserId(
            @Param("userId") Long userId,
            @Param("size") int size,
            @Param("offset") int offset
    );

    List<PointWithdrawRequest> findByStatus(
            @Param("status") WithdrawRequestStatus status,
            @Param("size") int size,
            @Param("offset") int offset
    );

    int updateStatusIfRequested(
            @Param("id") Long id,
            @Param("newStatus") WithdrawRequestStatus newStatus,
            @Param("processedBy") Long processedBy,
            @Param("rejectReason") String rejectReason,
            @Param("externalTxId") String externalTxId
    );
}
