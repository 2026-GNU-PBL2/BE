package pbl2.sub119.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.dto.request.OauthLoginRequest;
import pbl2.sub119.backend.auth.dto.response.AuthTokenDto;
import pbl2.sub119.backend.auth.service.OauthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class OauthController {

    private final OauthService oauthService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenDto> login(@RequestBody final OauthLoginRequest request) {
        return ResponseEntity.ok(oauthService.login(request));
    }
}
