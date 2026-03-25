package pbl2.sub119.backend.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import pbl2.sub119.backend.test.vo.TestVO;


@Mapper
public interface TestMapper {
    TestVO selectById(Long id);
}