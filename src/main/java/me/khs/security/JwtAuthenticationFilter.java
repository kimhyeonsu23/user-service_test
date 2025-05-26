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
			// getContext() : 현재 요청의 보안 컨텍스트(security context)를 가져오는 메서드 => 이 컨텍스트 안에는 로그인한 사용자의 인증 정보 (authentication)이 들어있음 -> 누가 요했는지 담은 객체를 꺼내기 위한 관문
			//securityContextHolder
			
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


/*
 * 전체 흐름 : http 요청 -> filterChain(여러 필터통과) -> securityContextHolder에 인증 정보 저장 -> controller 접근
 * 
 * getContext() : 현재 요청의 보안 컨텍스트를 가져오는 메서드
 * => 이 컨텍스트 안에는 로그인한 사용자의 인증 정보(authentication)이 담겨있음
 * => 누가 요청 했는가를 담은 객체를 꺼내기 위한 관문.
 * 
 * SecurityContextHolder : 스프링 시큐리티에서 현재 사용자의 인증 정보를 보관하는 저장소
 * => 이 안에는 securityContext 객체가 있고 이 객체 속에는 Authentication 객체가 있음
 * => 현재 요청의 인증 상태를 저장하고 꺼낼 수 있게 해주는 중앙보안 저장소
 *
 * HttpServletRequest : 사용자가 보낸 http 요청 정보를 담고 있는 객체사용자가 보낸 url, 헤더, 쿠키, 바디, 파라미터 등 모두 접근 가능
 *  => 클라이언트가 서버에 보낸 모든 요청 정보가 담긴 객체.
 *  요청 헤더에서 jwt 토큰을 꺼내기
 *  
 *  httpServletResponse : 서버가 클라이언트에게 응답을 보낸 사용하는 객체
 *  http 상태코드, 헤더 설정, 바디 출력 등에 사용됨
 *  => 서버가 클라이언트에게 응답을 구성하는데 사용하는 객체
 *  
 *  FilterChain : 여러개의 필터(보안, 로깅 등)을 연결하여 처리하는 체인
 *  현재 필터가 끝나면 다음 필터로 요청을 넘기기 위해 사용
 *  => 이걸 호출하지 않으면, 필터 체인 멈추고 뒤에 있는 컨트롤러에도 안감.
 *  요청을 다음 필터 또는 컨트롤러로 넘기는 '필터 이동 레일'
 *  
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
