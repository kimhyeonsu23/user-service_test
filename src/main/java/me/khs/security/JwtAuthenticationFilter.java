package me.khs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 이 필터는 사용자가 요청을 보낼때마다 -> 토큰을 꺼내고 -> 검증하고 -> 인증객체를 securityContext에 등록함.
@Component
@RequiredArgsConstructor //final 필드인 jwtTokenProvider를 자동으로 생성자 주입
public class JwtAuthenticationFilter extends OncePerRequestFilter { 
	// JwtAuthenticationFilter : 요청이 들어올 때마다 실행되는 spring security 필터, oncePerRequestFilter를 상속해서 요청마다 한번씩만 작동.
	
	private final JwtTokenProvider jwtTokenProvider; // jw.t 토큰을 생성, 파싱, 검증하는 도구.
	private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		
		String token = jwtTokenProvider.resolveToken(request);
		// 클라이언트의 http 요청에서 jwt 토큰을 꺼내옴. -> 문자열로 꺼냄.
		
		logger.debug("[JwtAuthenticationFilter] 추출한 토큰 : {}", token); // 콘솔에 토큰 출력하기.
		
		if (token != null && jwtTokenProvider.validateToken(token)) {
			// 토큰이 널이 아니고 유효한지 확인.
			
			Authentication auth = jwtTokenProvider.getAuthentication(token);
			// a.u.t.h : spring security가 이사람이 로그인되었다고 간주할 수 있는 객체임.
			//토큰에서 이메일을 통해 데이터베이스에서 찾아 userDetails를 만들고 이를 통해 authentication 객체를 생성함.
			
			SecurityContextHolder.getContext().setAuthentication(auth);
			//authentication 객체를 spring security의 전역보안 컨텍스트에 저장하는 코드 (이게있어야 컨트롤러에서 authenticaitonprincipal을 통해 유저 정보를 꺼낼 수 있음)
			
			logger.debug("[JwtAuthenticationFilter] 인증 완료 : ", auth.getName());
			
		} else {
			logger.debug("[JwtAuthenticationFilter] 토큰 없음 또는 유효하지 않음");
		}
		filterChain.doFilter(request, response); // 반드시 호출해야 함. 다음 필터로 요청을 넘겨주는 역할 (컨트롤러로 가는 길을 열어줌)
	}

}

/* 1. 클라이언트의 h.t.t.p 요청
 * 사용자가 보내는 요청을 중간에 이 필터가 가로채서 검사를 한 후에 다음 필터로 넘김.
 * --- JwtAuthenticationFilter ---
 * 
 * 2. resoulveToken() -> 요청 헤더에서 j.w.t 토큰 추출
 * 
 * 3. validateToken() -> 토큰의 유효성 검증
 * 
 * 4. getAuthentication() -> 토큰에서 사용자 정보 추출 -> 인증 객체 생성
 * 
 * 5. SecurityContextHolder.setAuthentication() -> 인증 완료 처리.
 * 
 * => 다음 필터 / 컨트롤러 처리.
 * */
