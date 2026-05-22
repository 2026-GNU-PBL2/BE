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

    void incrementMineCount(@Param("id") Long id);

    void incrementUnknownCount(@Param("id") Long id);

    void incrementResponseCount(@Param("id") Long id);

    List<DeviceDetectionEvent> findExpiredPending(@Param("now") java.time.LocalDateTime now);
}
