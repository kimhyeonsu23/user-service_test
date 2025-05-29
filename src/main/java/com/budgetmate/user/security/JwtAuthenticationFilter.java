package com.budgetmate.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	// 매 요청마다 실행. => 요청이 올때 실제로 인증을 수행.

    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)  // 매개변수는 서블릿 요청처리 파이프라인에서 자동 주입함.
            throws ServletException, IOException {
    	// HttpServeltRequeset : 클라이언트로부터 온 요청 정보를 담는 객체 : 요청 URL, 메서드(get,post), 헤더, 쿠키, 바디 등 접근 가능
    	// HttpServeltResponse : 서버가 클라이언트로 보내는 응답을 조작하는 객체. : 상태코드, 헤더, 바디 작성 가능.
    	// FilterChain : servlet 사양에 정의된 인터페이스 : spring security는 이를 상속받아서 필터 체인 구조를 구성
    	//				현재 필터에서 다음 

        String requestURI = request.getRequestURI();
        logger.debug("[JwtAuthenticationFilter] 요청 URI: {}", requestURI);

        if (
                requestURI.equals("/user/login") ||
                        requestURI.equals("/user/signup") ||
                        requestURI.equals("/user/send-code") ||         // 인증코드 요청 허용
                        requestURI.equals("/user/verify-code")          // 인증코드 검증 허용
        ) {
            logger.debug("[JwtAuthenticationFilter] 인증 예외 경로 - 필터 건너뜀: {}", requestURI);
            filterChain.doFilter(request, response); // 다음 필터를 실행하고 돌아온 후에 아래쪽 코드를 계속 실행함
            // response는 실제 응답이 작성되기 전의 응답 객체. response객체는 서블릿 컨테이너(톰캣 등)이 미리 생성해서 필터 체인에 넘겨줌. 필터가 실행될때는 컨트롤러가 실행되지 않았기 때문에 빈 응답 버퍼 + 200상태코드임.
           
            return;
        }


        String token = jwtTokenProvider.resolveToken(request);
        logger.debug("[JwtAuthenticationFilter] 추출한 토큰: {}", token);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            // SecurityContextHolder : spring Security가 사용하는 스레드 로컬 기반 인증 저장소 -> 한 요청에 대해 인증 객체를 저장하는 전역공간.
            // getContext() : 현재 요청 스레드의 security context 객체를 반환. => auth 객체는 되돌아가지 않고 단지 현재 요청 스레드 안의 전역 공간에 저장될 뿐.
            // setAuthentication(Authentication) : 현재 사용자 정보를 저장 (로그인 처리 후 호출)
            // getContext().getAuthentication() : 현재 인증된 사용자 정보 읽기용.
            
            logger.debug("[JwtAuthenticationFilter] 인증 완료 - 사용자: {}", auth.getName());
        }

        filterChain.doFilter(request, response); 
        // 현재 필터에서 다음 필터로 요청을 넘겨줌 -> 만약 doFilter를 호출하지 않으면 요청은 그 자리에서 멈춤
        // 최종적으로 필터 체인 끝에는 보통 dispatherServlet이 있고 이것이 @RestController를 호출함.
    }
}
// 요청 흐름 : 요청 -> SecurityFilterChain -> JwtAuthenticationFilter -> DispatcherServelt -> Controller...




