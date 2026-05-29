package pbl2.sub119.backend.concurrent.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.concurrent.entity.DeviceDetectionResponse;

@Mapper
public interface DeviceDetectionResponseMapper {

    void insert(DeviceDetectionResponse response);

    boolean existsByEventIdAndUserId(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId
    );

    int countByEventId(@Param("eventId") Long eventId);

    int countMineByEventId(@Param("eventId") Long eventId);

    int countUnknownByEventId(@Param("eventId") Long eventId);

    List<Long> findRespondedUserIdsByEventId(@Param("eventId") Long eventId);
}
