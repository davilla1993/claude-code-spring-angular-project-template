package com.gfolly.backend.iam.api;

import com.gfolly.backend.iam.application.*;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.shared.util.UserPrincipal;
import com.gfolly.backend.iam.api.dto.requests.ChangePasswordRequest;
import com.gfolly.backend.iam.api.dto.requests.SetupPasswordRequest;
import com.gfolly.backend.iam.api.dto.requests.CreateUserRequest;
import com.gfolly.backend.iam.api.dto.requests.UpdateUserRequest;
import com.gfolly.backend.iam.api.dto.responses.ResetEmployeePasswordResponse;
import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.infrastructure.mapper.UserMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.api.ApiResponse;
import com.gfolly.backend.shared.api.PageResponse;
import com.gfolly.backend.shared.util.ErrorMessages;
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

    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final SetupPasswordUseCase setupPasswordUseCase;
    private final ResetEmployeePasswordUseCase resetEmployeePasswordUseCase;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return userRepository.findByPublicIdWithCashRegister(principal.userId())
                .map(user -> ResponseEntity.ok(ApiResponse.success(UserMapper.toResponse(user, principal.tenantSlug()))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:view')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Role role) {
        var page = role != null
                ? userRepository.findAllByDeletedFalseAndRole(role, pageable)
                : userRepository.findAllByDeletedFalseWithCashRegister(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page.map(UserMapper::toResponse))));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = createUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable String id,
                                                             @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = updateUserUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String id) {
        deactivateUserUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.USER_DEACTIVATED));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable String id) {
        // Simple logic here: find, flip active, save
        var user = userRepository.findByPublicId(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));
        user.setActive(!user.getActive());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.message(user.getActive() ? ErrorMessages.USER_ACTIVATED : ErrorMessages.USER_DEACTIVATED));
    }

    @PutMapping("/me/setup-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> setupPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SetupPasswordRequest request) {
        setupPasswordUseCase.execute(principal.userId(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_CHANGED_SUCCESS));
    }

    @PutMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordUseCase.execute(principal.userId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_CHANGED_SUCCESS));
    }

    /**
     * OWNER/MANAGER réinitialise le mot de passe d'un employé.
     * Retourne le mot de passe temporaire en clair pour le communiquer à l'employé.
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<ResetEmployeePasswordResponse>> resetEmployeePassword(
            @PathVariable String id) {
        String tempPassword = resetEmployeePasswordUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(new ResetEmployeePasswordResponse(tempPassword)));
    }
}

