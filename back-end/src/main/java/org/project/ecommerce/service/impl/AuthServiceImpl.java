package org.project.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.ecommerce.constant.Role;
import org.project.ecommerce.dto.request.*;
import org.project.ecommerce.dto.response.AuthResponse;
import org.project.ecommerce.entities.PasswordResetToken;
import org.project.ecommerce.entities.RefreshToken;
import org.project.ecommerce.entities.User;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.PasswordResetTokenRepository;
import org.project.ecommerce.repository.RefreshTokenRepository;
import org.project.ecommerce.repository.UserRepository;
import org.project.ecommerce.security.jwt.JwtTokenProvider;
import org.project.ecommerce.service.AuthService;
import org.project.ecommerce.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    // ─── Reset token TTL: 15 minutes ─────────────────────────────────────────
    private static final long PASSWORD_RESET_TTL_MS = 15L * 60 * 1000;

    // ─── Register ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email đã được sử dụng", 409);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new CustomException("Email hoặc mật khẩu không đúng", 401);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Người dùng không tồn tại", 404));

        if (!user.isEnabled()) {
            throw new CustomException("Tài khoản đã bị khóa", 403);
        }

        // Revoke old refresh tokens and clean up
        refreshTokenRepository.revokeAllByUser(user);
        refreshTokenRepository.deleteExpiredAndRevokedByUser(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ─── Refresh Token ────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(rawToken)) {
            throw new CustomException("Refresh token không hợp lệ hoặc đã hết hạn", 401);
        }

        String tokenType = jwtTokenProvider.getTokenType(rawToken);
        if (!"refresh".equals(tokenType)) {
            throw new CustomException("Token không phải refresh token", 400);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new CustomException("Refresh token không tồn tại", 401));

        if (storedToken.isRevoked()) {
            throw new CustomException("Refresh token đã bị thu hồi", 401);
        }
        if (storedToken.isExpired()) {
            throw new CustomException("Refresh token đã hết hạn", 401);
        }

        // Rotate: revoke old, issue new pair
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        return buildAuthResponse(user);
    }

    // ─── Forgot Password ──────────────────────────────────────────────────────
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Luôn trả thành công để không lộ thông tin email tồn tại
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Xóa token cũ
            passwordResetTokenRepository.deleteAllByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(Instant.now().plusMillis(PASSWORD_RESET_TTL_MS))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = baseUrl + "/reset-password?token=" + token;
            sendPasswordResetEmail(user, resetLink);
            log.info("Password reset email sent to: {}", user.getEmail());
        });
    }

    // ─── Reset Password ───────────────────────────────────────────────────────
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Mật khẩu xác nhận không khớp", 400);
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new CustomException("Token đặt lại mật khẩu không hợp lệ", 400));

        if (resetToken.isUsed()) {
            throw new CustomException("Token đã được sử dụng", 400);
        }
        if (resetToken.isExpired()) {
            throw new CustomException("Token đặt lại mật khẩu đã hết hạn (15 phút)", 400);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Thu hồi tất cả refresh token sau khi đổi mật khẩu
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    // ─── Logout ───────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByToken(rawRefreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(rawRefreshToken)
                .user(user)
                .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .accessTokenExpiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .refreshTokenExpiresIn(jwtTokenProvider.getRefreshTokenExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    private void sendPasswordResetEmail(User user, String resetLink) {
        String subject = "Đặt lại mật khẩu - E-Commerce";
        String body = String.format("""
                Xin chào %s,

                Bạn đã yêu cầu đặt lại mật khẩu. Nhấn vào liên kết bên dưới để tiếp tục:

                %s

                Liên kết có hiệu lực trong 15 phút.

                Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.

                Trân trọng,
                E-Commerce Team
                """, user.getFullName() != null ? user.getFullName() : user.getEmail(), resetLink);

        emailService.sendSimpleEmail(user.getEmail(), subject, body);
    }
}
