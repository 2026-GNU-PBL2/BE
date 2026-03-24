package pbl2.sub119.backend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.submate.backend.auth.aop.Auth;
import pbl2.submate.backend.auth.entity.Accessor;
import pbl2.sub119.backend.user.controller.docs.UserDocs;
import pbl2.sub119.backend.user.dto.request.UserRequest;
import pbl2.sub119.backend.user.dto.response.UserResponse;
import pbl2.sub119.backend.user.dto.response.UserSignUpResponse;
import pbl2.sub119.backend.user.dto.response.UserUpdateResponse;
import pbl2.submate.backend.user.service.UserService;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController implements UserDocs {

    private final UserService userService;

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
}