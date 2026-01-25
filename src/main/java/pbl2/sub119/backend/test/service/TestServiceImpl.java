package pbl2.sub119.backend.test.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pbl2.sub119.backend.test.dto.TestResponse;
import pbl2.sub119.backend.test.mapper.TestMapper;
import pbl2.sub119.backend.test.vo.TestVO;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {


    private final TestMapper testMapper;


    @Override
    public TestResponse getTest(Long id) {
        TestVO vo = testMapper.selectById(id);
        return new TestResponse(vo.getName(), vo.getAmount());
    }
}
