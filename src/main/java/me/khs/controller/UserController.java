package me.khs.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.khs.dto.LoginRequest;
import me.khs.dto.SignupRequest;
import me.khs.dto.UserDto;
import me.khs.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	final UserService userService;
	
	public UserController (UserService userService) {
		
		this.userService = userService;
		
	}
	
	// 회원가입
	@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
	@PostMapping("/createUser")
	public ResponseEntity createUser(@RequestBody SignupRequest signupRequest) {
		
		//UserDto userDto = new UserDto(userName, password, email);
		//spr.ing의 의존성 주입으로 자동 생성해서 매개변수로 받음 -> new로 객체생성 할 필요가 없음.
		userService.createUser(signupRequest);
		return ResponseEntity.ok(signupRequest);
		
	}
	
	@PostMapping("/login")
	@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
	public ResponseEntity<?> login(@RequestBody LoginRequest request) { // 요청의 http body에 담긴 json을 loginRequest객체로 자동으로 변환.
		
		String token = userService.login(request);
		return ResponseEntity.ok().body(
				java.util.Map.of("token", token)
		);
		
	}
	
	@GetMapping("/me")
	@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
	public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
		// authenticationPrincipal User userDetils :spring security가 자동으로 현재 로그인한 사용자 정보를 userDetail에 넣어줌.
		// 이건 jwt필터가 securityContextHolder에 인증 정보를 넣어줬기 때문에 가능함.
		
		return ResponseEntity.ok( // http 상태코드 200을 가진 응답을 만든다는 의미
				Map.of( // 응답 body에 {"token" : token}이런 json을 넣는다는 뜻
						//map.of()는 불변 map을 만드는 정적메서드. 
						
					"username", userDetails.getUsername(),
					// 
					"authorities", userDetails.getAuthorities()
					//Map<String, String> map = new HashMap<>();
					//map.put("token", token);
					//return ResponseEntity.ok().body(map);
				)
		);
				
	}
	
	//responseEntity는 spring이 기본으로 제공하는 프레임워크 클래스 : http 응답을 만들 때 사용하는 클래스.
	// <?> 는 어떤 타입이든 될수 있다는 뜻. 즉 제네릭 임.(타입 추론)
	
//	@PostMapping("/login")
//	public String logIn(@RequestBody LoginDto loginDto) {
//		
//		boolean result = userService.login(userDto.getEmail(), userDto.getPassword());
//		
//		if (result) {
//			
//			return "login success !";
//			
//		}
//		else {
//			
//			return "login error !";
//			
//		}
//	
//		
//	}

}
