package com.budgetmate.user.config;

import com.budgetmate.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration	// spring 설정 클래스로 등록.
@EnableWebSecurity	// spring security 기능 활성화.
@RequiredArgsConstructor	// final 필드 생성자 주입 처리.
public class SecurityConfig { // spirng security는 기존 필터의 상대적 위치 기반 설정만 허용함. (절대순서 번호 지정 방식은 내부적 허용하지 않음)
// 앱 부팅 시 한번만 실행. => 어떻게 인증할지 등록하는 것.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	// HttpSecurity : spring security가 제공하는 보안 정책 설정을 위한 DSL 객체. : 필터체인, CORS/CSRF, 인증정책... 등을 구성할 수 있음.
    	// SecurityFilterChain : spring security 보안의 진짜 실행 단위. 요청이 들어올때마다 적용되는 필터들의 실제 집합체 -> 매 요청마다 실행됨.
    	
        http
                //.cors(Customizer.withDefaults()) //  CORS 허용 (cross-origin resource sharing)
                //위의 cors(Customuzer.withDefaults()가 작동은 하지만 제대로 동작하려면 CorsConfigurationSource가 Bean에 등록되어 있어야 함.
                // 이전에는 .cors()만 했지 정작 어떤 origin을 허용할지, 어떤 methods, header 들을 허용할지 설정이 없어서 options preflight 요청에 403 에러가 뜨게 됨.
                // Customizer.withDefaults()는 디폴트로 빈에 등록된 conrsConfigurationSource를 찾아서 그 설정을 사용하라는 뜻. -> 별도로 corsConfigurationSource를 설정하지 않으면 쓸 설정 정보가 없어서 에러 혹은 403
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())	// CSRF 방지 기능 비활성화 -> jwt 기반 인증에서는 서버가 세션을 만들지 않기 때문에 필요 없음.
                .httpBasic(httpBasic -> httpBasic.disable())	// HTTP Basic 인증 : id/pw를 http헤더에 담아 전송하는 기본 인증 방식 -> jwt 구조에서는 필요없
                .formLogin(form -> form.disable())	// 자체 로그인 페이지 비활성화. -> 자체적으로 jwt로그인 처리를 하기 때문에 폼로그인 불필요.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // sessionManagement() : 서버가 세션을 설정하지 않도록 설정. 매 요청마다 jwt만으로 인증 -> stateLess 방식.
                
                .authorizeHttpRequests(auth -> auth 	// authorizeHttpRequests()
                        .requestMatchers(	// requestMatcher().permitAll() : 특정 경로에 대해 인증 없이 접근 허용 =>로그인, 인증코드 전송...
                                "/user/signup", "/user/login",
                                "/user/send-code", "/user/verify-code",
                                "/user/oauth/kakao","/user/oauth/google","/user/confirm-social"
                        ).permitAll()
                        .anyRequest().authenticated() // 나머지 모든 요청은 jwt 인증 필요.
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // addFilterBefore 에 jwtAuthenticationFilter가 등록됨.
        // jwtAuthenticationFilter를 usernamePasswordAuthenticationFilter 앞에 등록 -> 로그인 필터 전에 jwt를 해석해서 securitycontextHolder에 인증 정보를 세팅해야하기 때문.
        // UsernamePasswordAuthenticationFilter : spring security의 기본 로그인 처리 필터. -> 우리는 usernamePasswordAuthenticationFilter를 사용하진 않지만 적당한 기준 위치를 잡기 위한 것.의미상 위친 기준이지 필터를 활성화 하는건 아님.
        //.class 가 붙는 이유 : 클래스 타입(클래스 객체)를 명시적으로 넘길때 붙임. 클래스 그 자체를 의미하는 타입 정보.

        return http.build(); // 위에서 설정한 보안 규칙을 종합해서 securityFilterChain 객체를 생성함. 이걸 spring security에 빈으로 등록하면 실제요청 처리 시 이 필터체인이 적용됨. 
    }
    
    
//    // 이 메서드만 없었을 때 -> cors 에러가 뜸.
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:5173"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
    // 아니야 게이트웨이를 끼고 하니까 오히려 더 에러가 뜸. 게이트웨이의 application.yml에서 cors처리를 하고 여기서도 처리를 하니까 양쪽에서 각각 cors헤더를 추가해서 응답에 cors 헤더가 각각 추가되어서
    // 응답에 두개가 겹쳐서 들어가게 됨. -> cors처리는 오직 게이트웨이서만 하기로 함. 게이트웨이가 cors 헤더를 붙이니까 user에서는 붙이지 ㅇ낳기로함. 
    // 브라우저는 cors에 매우 엄격한 보안 정책을 적용하므로 응답헤더에 acess-control-allow-origin이 2개이상 들어가면 바로 에러를 발생시킴.
    // 브라우저는 서버가 정확하게 하나의 오리진만 허용하기를 바람. -> 확인하려면 실제 크롬 네트워크 탭에서 확인해볼것.
    


    @Bean
    public PasswordEncoder passwordEncoder() { // spring security에서 passwordEncoder 인터페이스 타입으로 BCrypt 구현체를 반환. => 사용자가 입력한 비밀번호를 암호화해서 데이터베이스에 들어있는 암호화된 비밀번호와 비교함.
    	// PasswordEncoder : 인터페이스 : 비밀번호 암호화/검증 전략을 정의.
        return new BCryptPasswordEncoder(); //가장 널리 사용되는 구현체. 강력한 해시 알고리즘
        // BCrptPasswordEncoder 는 이 PasswordEncoder를 implements 한 클래스. 이미 구현하고 있음
    }
}

// 요청 처리 순서 정리
/* 
 * 1. spring boot 앱에 실행
 * 2. @configuration + @EnableWebSecurity로 securityConfig가 실행됨. (configuration이 붙은 클래스는 앱 시작시, 서버가 부팅 될 때 먼저 실행이 됨. 요청이 들어올때마다 실행되는건 아님(한번 실행됨)
 * 3. filterChain메서드 실행 -> spring securityr가 실행할 전체 보안 정책 + 필터 순서가 설정됨.
 */

// httpBasic() vs UsernamePasswordAuthenticationFilter의 차이점 -> 완전히 다름.
// 

//mvc -> CorsConfigurationSource, WebFlux -> CorsWebFilter 가 대응됨. 메서드 이름은 자유지만 리턴 타입이 CorsConfigurationSource이면 spring security가 자동으로 찾아서 등록해줌.
// CorsConfigurationSource : 인터페이스타입 : 요청에 따라 cors정책을 반환하는 역할.






