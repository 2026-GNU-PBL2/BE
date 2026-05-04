package pbl2.sub119.backend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

import pbl2.sub119.backend.user.controller.docs.UserDocs;
import pbl2.sub119.backend.user.dto.request.PhoneVerificationConfirmRequest;
import pbl2.sub119.backend.user.dto.request.PhoneVerificationRequest;
import pbl2.sub119.backend.user.dto.request.UserRequest;
import pbl2.sub119.backend.user.dto.response.DuplicateCheckResponse;
import pbl2.sub119.backend.user.dto.response.UserResponse;
import pbl2.sub119.backend.user.dto.response.UserSignUpResponse;
import pbl2.sub119.backend.user.dto.response.UserUpdateResponse;
import pbl2.sub119.backend.user.service.PhoneVerificationService;
import pbl2.sub119.backend.user.service.UserService;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController implements UserDocs {

    private final UserService userService;
    private final PhoneVerificationService phoneVerificationService;

    @Override
    @PostMapping
    public ResponseEntity<UserSignUpResponse> signUp(
            @Auth final Accessor accessor,
            @RequestBody @Valid final UserRequest request
    ) {
        return ResponseEntity.ok(userService.signUp(accessor, request));
    }

    @Override
    @GetMapping
    public ResponseEntity<UserResponse> find(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(userService.findUser(accessor));
    }

    @Override
    @PatchMapping
    public ResponseEntity<UserUpdateResponse> update(
            @Auth final Accessor accessor,
            @RequestBody @Valid final UserRequest request
    ) {
        return ResponseEntity.ok(userService.update(accessor, request));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> withdraw(
            @Auth final Accessor accessor
    ) {
        userService.withdraw(accessor);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/check/email")
    public ResponseEntity<DuplicateCheckResponse> checkEmail(
            @Auth final Accessor accessor,
            @RequestParam final String email
    ) {
        return ResponseEntity.ok(userService.checkEmail(email));
    }

    @Override
    @GetMapping("/check/nickname")
    public ResponseEntity<DuplicateCheckResponse> checkNickname(
            @Auth final Accessor accessor,
            @RequestParam final String nickname
    ) {
        return ResponseEntity.ok(userService.checkNickname(nickname));
    }

    @Override
    @PostMapping("/phone/verify/request")
    public ResponseEntity<Void> requestPhoneVerification(
            @Auth final Accessor accessor,
            @RequestBody @Valid final PhoneVerificationRequest request
    ) {
        phoneVerificationService.sendOtp(accessor.getUserId(), request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/phone/verify/confirm")
    public ResponseEntity<Void> confirmPhoneVerification(
            @Auth final Accessor accessor,
            @RequestBody @Valid final PhoneVerificationConfirmRequest request
    ) {
        phoneVerificationService.confirm(request.phoneNumber(), request.code());
        return ResponseEntity.ok().build();
    }
}
