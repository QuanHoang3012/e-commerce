package org.project.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.ecommerce.base.BaseController;
import org.project.ecommerce.base.BaseResponse;
import org.project.ecommerce.dto.request.*;
import org.project.ecommerce.dto.response.AuthResponse;
import org.project.ecommerce.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký / Đăng nhập / Quản lý token / Quên mật khẩu")
public class AuthController extends BaseController {

    private final AuthService authService;

    // ─── Register ─────────────────────────────────────────────────────────────
    @Operation(summary = "Đăng ký tài khoản Customer")
    @PostMapping("/register")
    public BaseResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return wrapSuccess(authService.register(request));
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public BaseResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return wrapSuccess(authService.login(request));
    }

    // ─── Refresh Token ────────────────────────────────────────────────────────
    @Operation(summary = "Làm mới access token bằng refresh token")
    @PostMapping("/refresh-token")
    public BaseResponse<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        return wrapSuccess(authService.refreshToken(request));
    }

    // ─── Forgot Password ──────────────────────────────────────────────────────
    @Operation(summary = "Quên mật khẩu — gửi email đặt lại")
    @PostMapping("/forgot-password")
    public BaseResponse<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return wrapSuccess(null);
    }

    // ─── Reset Password ───────────────────────────────────────────────────────
    @Operation(summary = "Đặt lại mật khẩu với token từ email")
    @PostMapping("/reset-password")
    public BaseResponse<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return wrapSuccess(null);
    }

    // ─── Logout ───────────────────────────────────────────────────────────────
    @Operation(summary = "Đăng xuất — thu hồi refresh token")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return wrapSuccess(null);
    }
}

