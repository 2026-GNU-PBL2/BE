package pbl2.sub119.backend.test.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.test.dto.TestResponse;
import pbl2.sub119.backend.test.service.TestService;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {


    private final TestService testService;


    @GetMapping("/{id}")
    public TestResponse getTest(@PathVariable Long id) {
        return testService.getTest(id);
    }
}