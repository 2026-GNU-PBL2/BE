package pbl2.sub119.backend.mail.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.mail.entity.ReceivedMail;

import java.util.List;

@Mapper
public interface ReceivedMailMapper {

    List<ReceivedMail> findByUserId(@Param("userId") Long userId);

    ReceivedMail findByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
}