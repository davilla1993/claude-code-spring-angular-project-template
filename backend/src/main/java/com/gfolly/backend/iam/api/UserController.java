package com.gfolly.backend.iam.api;

import com.gfolly.backend.iam.api.dto.requests.ChangePasswordRequest;
import com.gfolly.backend.iam.api.dto.requests.CreateUserRequest;
import com.gfolly.backend.iam.api.dto.requests.SetupPasswordRequest;
import com.gfolly.backend.iam.api.dto.requests.UpdateUserRequest;
import com.gfolly.backend.iam.api.dto.responses.TemporaryPasswordResponse;
import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.application.*;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.shared.api.ApiResponse;
import com.gfolly.backend.shared.api.PageResponse;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.shared.util.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final SetUserActiveUseCase setUserActiveUseCase;
    private final ResetUserPasswordUseCase resetUserPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final SetupPasswordUseCase setupPasswordUseCase;

    // --- Compte courant ---

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(getUserUseCase.execute(principal.userId())));
    }

    @PutMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordUseCase.execute(principal.userId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_CHANGED_SUCCESS));
    }

    @PutMapping("/me/setup-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> setupPassword(@AuthenticationPrincipal UserPrincipal principal,
                                                           @Valid @RequestBody SetupPasswordRequest request) {
        setupPasswordUseCase.execute(principal.userId(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_CHANGED_SUCCESS));
    }

    // --- Administration ---

    @GetMapping
    @PreAuthorize("hasAuthority('user:view')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(listUsersUseCase.execute(role, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:view')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(getUserUseCase.execute(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createUserUseCase.execute(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<UserResponse>> update(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable String id,
                                                            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(updateUserUseCase.execute(principal.userId(), id, request)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> activate(@AuthenticationPrincipal UserPrincipal principal,
                                                      @PathVariable String id) {
        setUserActiveUseCase.execute(principal.userId(), id, true);
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.USER_ACTIVATED));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable String id) {
        setUserActiveUseCase.execute(principal.userId(), id, false);
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.USER_DEACTIVATED));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<TemporaryPasswordResponse>> resetPassword(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                new TemporaryPasswordResponse(resetUserPasswordUseCase.execute(id))));
    }
}
