package com.budgetmate.user.service;

import com.budgetmate.user.dto.*;
import com.budgetmate.user.entity.LoginType;
import com.budgetmate.user.entity.User;
import com.budgetmate.user.repository.UserRepository;
import com.budgetmate.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    public User signup(SignupRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUserName())
                .roles(List.of("ROLE_USER"))
                .build();
        return userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRoles());
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public SocialLoginResult googleLoginAndGetUser(String code) {
        String token = getGoogleAccessToken(code);
        GoogleUserInfo g = getGoogleUserInfo(token);
        return processSocialLogin(toSocialUserInfo(g), LoginType.GOOGLE);
    }

    public SocialLoginResult kakaoLoginAndGetUser(String code) {
        String token = getAccessToken(code);
        KakaoUserInfo k = getKakaoUserInfo(token);
        return processSocialLogin(toSocialUserInfo(k), LoginType.KAKAO);
    }

    public User confirmGoogleLink(String code) {
        String token = getGoogleAccessToken(code);
        GoogleUserInfo g = getGoogleUserInfo(token);
        return confirmSocialLink(toSocialUserInfo(g), LoginType.GOOGLE);
    }

    public User confirmKakaoLink(String code) {
        String token = getAccessToken(code);
        KakaoUserInfo k = getKakaoUserInfo(token);
        return confirmSocialLink(toSocialUserInfo(k), LoginType.KAKAO);
    }

    public SocialLoginResult processSocialLogin(SocialUserInfo info, LoginType loginType) {
        Optional<User> userOpt = userRepository.findBySocialIdAndLoginType(info.getId(), loginType);
        if (userOpt.isPresent()) {
            return SocialLoginResult.builder().user(userOpt.get()).requiresConsent(false).build();
        }

        Optional<User> existingEmailUser = userRepository.findByEmail(info.getEmail());
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();
            if (user.getLoginType() == LoginType.LOCAL) {
                return SocialLoginResult.builder().user(user).requiresConsent(true).build();
            } else {
                return SocialLoginResult.builder().user(user).requiresConsent(false).build();
            }
        }

        User newUser = User.builder()
                .email(info.getEmail())
                .userName(info.getName())
                .loginType(loginType)
                .socialId(info.getId())
                .roles(List.of("ROLE_USER"))
                .build();

        return SocialLoginResult.builder()
                .user(userRepository.save(newUser))
                .requiresConsent(false)
                .build();
    }

    public User confirmSocialLink(SocialUserInfo info, LoginType loginType) {
        User user = userRepository.findByEmail(info.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (user.getLoginType() != LoginType.LOCAL) {
            throw new RuntimeException("이미 소셜 계정으로 등록된 사용자입니다.");
        }

        user.setLoginType(loginType);
        user.setSocialId(info.getId());
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일의 사용자가 존재하지 않습니다."));
    }

    private String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().get("access_token") != null) {
                return (String) response.getBody().get("access_token");
            } else {
                throw new RuntimeException("카카오 access_token 발급 실패: " + response.getBody());
            }

        } catch (HttpClientErrorException.BadRequest e) {
            throw new RuntimeException("이미 사용된 인가 코드입니다.");
        }
    }

    private String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", googleRedirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://oauth2.googleapis.com/token", request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private GoogleUserInfo getGoogleUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo", HttpMethod.GET, request, Map.class);

        Map<String, Object> body = response.getBody();

        return GoogleUserInfo.builder()
                .id((String) body.get("id"))
                .email((String) body.get("email"))
                .name((String) body.get("name"))
                .build();
    }

    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, Map.class);

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return KakaoUserInfo.builder()
                .id(String.valueOf(response.getBody().get("id")))
                .email((String) kakaoAccount.get("email"))
                .nickname((String) profile.get("nickname"))
                .build();
    }

    private SocialUserInfo toSocialUserInfo(GoogleUserInfo g) {
        return SocialUserInfo.builder()
                .id(g.getId())
                .email(g.getEmail())
                .name(g.getName())
                .build();
    }

    private SocialUserInfo toSocialUserInfo(KakaoUserInfo k) {
        return SocialUserInfo.builder()
                .id(k.getId())
                .email(k.getEmail())
                .name(k.getNickname())
                .build();
    }
}
