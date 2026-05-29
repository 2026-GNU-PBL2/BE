package pbl2.sub119.backend.concurrent.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.concurrent.entity.DeviceDetectionEvent;
import pbl2.sub119.backend.concurrent.enumerated.DeviceDetectionStatus;

@Mapper
public interface DeviceDetectionMapper {

    void insert(DeviceDetectionEvent event);

    DeviceDetectionEvent findById(@Param("id") Long id);

    List<DeviceDetectionEvent> findByPartyId(@Param("partyId") Long partyId);

    void updateStatus(
            @Param("id") Long id,
            @Param("status") DeviceDetectionStatus status
    );

    DeviceDetectionEvent findByIdForUpdate(@Param("id") Long id);

    // 상태 전환 1회 보장: PENDING 상태일 때만 업데이트, 영향 행 수 반환
    int updateStatusIfPending(@Param("id") Long id, @Param("status") DeviceDetectionStatus status);

    List<DeviceDetectionEvent> findExpiredPending(@Param("now") java.time.LocalDateTime now);
}
