package pbl2.sub119.backend.concurrent.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.concurrent.entity.PartyWarningHistory;

@Mapper
public interface PartyWarningHistoryMapper {

    void insert(PartyWarningHistory history);

    List<PartyWarningHistory> findByPartyId(@Param("partyId") Long partyId);

    int countByPartyId(@Param("partyId") Long partyId);
}
