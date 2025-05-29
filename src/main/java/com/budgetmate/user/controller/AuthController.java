package com.budgetmate.user.controller;

import com.budgetmate.user.dto.LoginRequest;
import com.budgetmate.user.dto.SignupRequest;
import com.budgetmate.user.dto.SocialLoginResult;
import com.budgetmate.user.dto.SocialUserInfo;
import com.budgetmate.user.entity.LoginType;
import com.budgetmate.user.entity.User;
import com.budgetmate.user.security.CustomUserDetails;
import com.budgetmate.user.security.JwtTokenProvider;
import com.budgetmate.user.service.EmailService;
import com.budgetmate.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    private final Map<String, VerificationInfo> verificationCodes = new ConcurrentHashMap<>();
    private static final long CODE_EXPIRE_TIME = 5 * 60 * 1000;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("[AuthController] /send-code 요청 들어옴 → {}", email);

        if (userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이미 가입된 이메일입니다."
            ));
        }

        String code = emailService.sendVerificationCode(email);
        verificationCodes.put(email, new VerificationInfo(code));
        return ResponseEntity.ok(Map.of("success", true, "message", "이메일 전송 완료"));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String inputCode = request.get("code");

        VerificationInfo info = verificationCodes.get(email);
        if (info == null) {
            return ResponseEntity.badRequest().body(Map.of("verified", false, "message", "인증 요청 내역이 없습니다."));
        }

        if (info.isExpired(CODE_EXPIRE_TIME)) {
            verificationCodes.remove(email);
            return ResponseEntity.badRequest().body(Map.of("verified", false, "message", "인증코드가 만료되었습니다."));
        }

        if (!info.getCode().equals(inputCode)) {
            return ResponseEntity.badRequest().body(Map.of("verified", false, "message", "인증코드가 일치하지 않습니다."));
        }

        verificationCodes.remove(email);
        return ResponseEntity.ok(Map.of("verified", true));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이미 등록된 이메일입니다."
            ));
        }

        User newUser = userService.signup(request);
        String token = jwtTokenProvider.createToken(newUser.getId(), newUser.getEmail(), newUser.getRoles());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", newUser,
                "token", token
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) {
        try {
            SocialLoginResult result = userService.kakaoLoginAndGetUser(code);

            if (result.isRequiresConsent()) {
                return ResponseEntity.ok(Map.of(
                        "requiresConsent", true,
                        "email", result.getUser().getEmail(),
                        "userName", result.getUser().getUserName()
                ));
            }

            String token = jwtTokenProvider.createToken(result.getUser().getId(), result.getUser().getEmail(), result.getUser().getRoles());
            return ResponseEntity.ok(Map.of(
                    "accessToken", token,
                    "email", result.getUser().getEmail(),
                    "userName", result.getUser().getUserName()
            ));
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of("error", "카카오 로그인 실패"));
        }
    }

    @GetMapping("/oauth/google")
    public ResponseEntity<?> googleLogin(@RequestParam String code) {
        try {
            SocialLoginResult result = userService.googleLoginAndGetUser(code);

            if (result.isRequiresConsent()) {
                return ResponseEntity.ok(Map.of(
                        "requiresConsent", true,
                        "email", result.getUser().getEmail(),
                        "userName", result.getUser().getUserName()
                ));
            }

            String token = jwtTokenProvider.createToken(result.getUser().getId(), result.getUser().getEmail(), result.getUser().getRoles());
            return ResponseEntity.ok(Map.of(
                    "accessToken", token,
                    "email", result.getUser().getEmail(),
                    "userName", result.getUser().getUserName()
            ));
        } catch (Exception e) {
            log.error("구글 로그인 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", "구글 로그인 실패"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "userName", user.getUserName(),
                "roles", user.getRoles()
        ));
    }
    @PostMapping("/confirm-social")
    public ResponseEntity<?> confirmSocial(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String loginTypeStr = request.get("loginType");

        User user = userService.findByEmail(email);
        Long id = user.getId();

        if (user.getLoginType() != LoginType.LOCAL) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 소셜 로그인 계정입니다."));
        }

        LoginType typeToUpdate;
        try {
            typeToUpdate = LoginType.valueOf(loginTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "유효하지 않은 로그인 타입입니다."));
        }

        User updated = userService.confirmSocialLink(
                SocialUserInfo.builder()
                        .id(user.getSocialId()) // ← 이건 프론트에서 넘겨줄 수도 있음
                        .email(user.getEmail())
                        .name(user.getUserName())
                        .build(),
                typeToUpdate
        );

        String token = jwtTokenProvider.createToken(id, updated.getEmail(), updated.getRoles());
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "email", updated.getEmail(),
                "userName", updated.getUserName()
        ));
    }

    private static class VerificationInfo {
        private final String code;
        private final long createdAt;

        public VerificationInfo(String code) {
            this.code = code;
            this.createdAt = System.currentTimeMillis();
        }

        public boolean isExpired(long timeoutMillis) {
            return System.currentTimeMillis() - createdAt > timeoutMillis;
        }

        public String getCode() {
            return code;
        }
    }
}
