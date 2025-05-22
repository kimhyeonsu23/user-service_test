package me.khs.security;
import me.khs.service.UserDetailsServiceImpl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final UserDetailsServiceImpl userDetailsService;

	@Value("${jwt.secret}")
	private String jwtKey;
	private Long userId;
	private final long tokenValidTime = 1000L * 60 * 60; // 1시간

	private SecretKey key; // SecretKey : 비밀키 secretKey를 나타내는 자바 표준 인터페이스.

	private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

	@PostConstruct
	protected void init() {
		// Base64 인코딩 후 SecretKey 생성
		String encodedKey = Base64.getEncoder().encodeToString(jwtKey.getBytes(StandardCharsets.UTF_8));
		// jwtKey를 utf-8 바이트 배열로 바꾸고 그 바이트 배열을 base64 문자열로 인코딩.
		key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedKey));
		// 위에서 만든 encodedKey(base64 문자열)을 다시 base64 디코딩 해서 바이트 배열로 바꾸고 hmacShaKeyFor()에 넣어서  secretKey객체로 만듦.
		//hmacShaKeyFor() 는 hmac 알고리즘을 사용할 수 있는 secretKEy객체를 생성해주는 함수. -> 진짜 비밀키 객체를 만듦.
	}

	//  토큰 생성
	public String createToken(Long userId, String email, List<String> roles) {
		Date now = new Date(); // 시간은 현재 시간
		Date expiry = new Date(now.getTime() + tokenValidTime); // 만료시간

		return Jwts.builder() // jwt를 생성할 수 있는 빌더 객체.
				.subject(email) // subject 설정 (토큰의 주체)
				.claim("roles", roles) // roles claim 설정 {"roles" : ["ROLE_USER", "ROLE_ADMIN"]}
				.claim("userId", userId)
				.issuedAt(now)  // 발근 시간 설정
				.expiration(expiry)  // 만료 시간설정
				.signWith(key)  // jwt의 서명(signature) 부분을 생성함.
				.compact();  // jwt를 문자열 형태로 최종 변환, 문자 -> 브라우저나 클라이언트에 전달되는 jwt.
	}

	//  인증 객체 생성.
	public Authentication getAuthentication(String token) {
		//authentication : spring security에서 인증된 사용자를 나타내는 인터페이스
		//usernamePasswordAuthenticationToken : 위 인터페이스를 구현한 가장 많이 쓰는 클래스
		String email = getEmail(token);
		Long userId = getUserId(token);
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		// 이메일을 기준으로 데이터베이스에서 정보를 조회.
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
		//jwt를 spring security에서 인증된 객체로 바꿔주는 과정.
		//usernamePasswordAuthenticationToken : 인증객체(authentication)을 생성하는 클래스
	}

	public String getEmail(String token) {
		return Jwts.parser().verifyWith(key)
				//토큰을 해석할 파서를 만들고 서명 검증을 위한 비밀키를 등록.
							.build().parseSignedClaims(token) 
// 설정한 파서를 바탕으로 jwt토큰을 파싱함. 토큰을 header.payload.signatur로 나눠서 signature가 key로 서명한 것과 일치하는지 검증
							.getPayload()	// jwt의 본문 내용을 가져
							.getSubject();
	}
	
	public Long getUserId(String token) {
		
		Claims claims = Jwts.parser().verifyWith(key)
							.build().parseSignedClaims(token)
							.getPayload();
		return claims.get("userId", Long.class);
		
	}

	// ✅ 토큰 유효성 검사
	public boolean validateToken(String token) {
		try {
			Jws<Claims> claims = Jwts.parser().verifyWith(key) //서명을 key로 검증함.
			//jws<Claims>는 서명된 jwt이고 그 안에 claims를 담고 있는 구조.
											.build().parseSignedClaims(token);
			// Jws<Claims> = jws : 서명이 포함된 jwt / claims : 토큰 안에 들어있는 실제 정보들
			
			return !claims.getPayload().getExpiration().before(new Date());
			//토큰에 들어있는 만료시간을 꺼내서 지금 시간이 그 만료시간보다 자나갔는지 확인.
			
		} catch (JwtException | IllegalArgumentException e) {
			// 토큰이 조작되었거나 기간이 이상하면 여기서 처리.
			logger.warn(" 유효하지 않은 토큰: {}", e.getMessage());
			return false;
			
		}
	}

	// 🔍 HTTP 요청에서 토큰 추출
	public String resolveToken(HttpServletRequest request) {
		//클라이언트가 요청을 보낼때 http헤더에 담은 jwt 토큰을 꺼내오는 함수.
		return request.getHeader("X-AUTH-TOKEN");
		
	}
}

// header : 어떤 알고리즘으로 서명했는지
// payload : 실제 정보
// signature : 서명 (위조 방지용)