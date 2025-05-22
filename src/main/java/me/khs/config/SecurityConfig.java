package me.khs.config;

import lombok.RequiredArgsConstructor;
import me.khs.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration	// 설정용 클래
@EnableWebSecurity	// spring security 기능 켜기
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Bean
	public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
		
		http
			.csrf (csrf -> csrf.disable())	// csrf 보안 비활성화 (restApi에는 보통 필요없음)
			.httpBasic(Customizer.withDefaults())	// 기본 인증 비활성화
			.formLogin(form -> form.disable())	//폼 로그인 꺼두기
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// 세션을 아예 만들지 않음. -> 서버가 아무 정보도 기억하지 않음. 매 요청마다 토큰을 보내서 서버가 해석하여 사용자 확인. // 여기까지가 필수조건.
			// 세션은 로그인한 사용자의 정볼르 임시로 기억하는 공간. 서버가 로그인 정보를 메모리에 저장함. (서버가 로그인 상태를 기억) -> 세션이라는 저장소.
			// 클라이언트는 세션 id(쿠키로 전달)을 매 요청마다 보내고 서버는 그걸 보고 누군지 알게 됨.
			.authorizeHttpRequests(auth -> auth
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ OPTIONS 허용
					// http options 메서드로 들어오는 모든 요청 (/**) 을 인증없이 허용하겠다는 뜻.
					
		            .requestMatchers("/user/signup", "/user/login", "/user/createUser").permitAll() // ✅ 인증 없이 가능한 경로
		            // 이 3개의 경로로 들어오는 요청은 모두 인증 없이 접근가능하도록 허용하겠다는 의미.
		            
		            .anyRequest().authenticated() // 나머지는 인증이 필요.
		            
		        )
			// 회원가입, 로그인은 인증없이허용
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

			// 모든 요청에 대해 usrnamePasswordAuthenticiatoinFilter 이전에 jwtAuthenticaitonFilter를 동작하게 등록함.
			// 클라이언트가 "Authorization: Bearer <token>"을 붙여서 보내면 jwtAuthenticaitonFilter 필터가 먼저 실행되고 
			// -> 토큰을 해석해서 authentica.iton 객체를 만듦.
			// -> 전역 봉나 컨텍스트 (SecurityContextHolder)에 등록해줌.
			// -> 이게 있어야 나중에 @AuthenticaitonPrincipal도 잘 동작함.
		
		return http.build();
		
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		
		return new BCryptPasswordEncoder();
		
	}

}

// RestApi : 웹 브라우저가 아닌 프로그램끼리 통신하는 방식. 자원을 url로 표현하고 동작을 http 메서드로 표현.
// .class : 자바에서 클래스 자체(정의)를 나타내는 특별한 객체. 이 클래스 자체를 참조하는것. 클래스의 인스턴스(객체)가 아니라 클래스 정보 자체를 넘김.
// addFilterBefore(필터1, 필터2) : 필터 2 앞에 필터1을 실행시키라는 뜻.
// 브라우저는 cors 요청 전에 options 요청(preflight)을 자동으로 보냄 -> 이걸 spring security가 인증 필요하다고 막으면 에러가 뜸.
