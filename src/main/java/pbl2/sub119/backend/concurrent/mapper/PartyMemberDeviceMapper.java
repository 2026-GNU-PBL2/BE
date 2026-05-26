package pbl2.sub119.backend.concurrent.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.concurrent.dto.response.PartyMemberDeviceResponse;
import pbl2.sub119.backend.concurrent.entity.PartyMemberDevice;

@Mapper
public interface PartyMemberDeviceMapper {

    void insert(PartyMemberDevice device);

    List<PartyMemberDevice> findByUserId(@Param("userId") Long userId);

    List<PartyMemberDevice> findByPartyId(@Param("partyId") Long partyId);

    List<PartyMemberDeviceResponse> findByPartyIdWithUserInfo(@Param("partyId") Long partyId);

    PartyMemberDevice findByUserIdAndPartyId(
            @Param("userId") Long userId,
            @Param("partyId") Long partyId
    );
}
