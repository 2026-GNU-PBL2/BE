package pbl2.sub119.backend.concurrent.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.concurrent.entity.UserViolationRecord;

@Mapper
public interface UserViolationRecordMapper {

    void insert(UserViolationRecord record);

    List<UserViolationRecord> findByUserId(@Param("userId") Long userId);

    List<UserViolationRecord> findByPartyId(@Param("partyId") Long partyId);
}
