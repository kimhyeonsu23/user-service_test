//package me.khs.controller;
//
//import java.util.Map;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import me.khs.entity.UserEntity;
//import me.khs.service.UserService;
//import me.khs.token.JwtUtil;
//
//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//	
//	private final UserService userService;
//	private final JwtUtil jwtUtil;
//	
//	public AuthController(UserService userService, JwtUtil jwtUtil) {
//		
//		this.userService = userService;
//		this.jwtUtil = jwtUtil;
//		
//	}
//	
//	@PostMapping("/login")
//	public ResponseEntity<?> login (@RequestParam String email,
//									@RequestParam String password) {
//		
//		UserEntity user = userService.authenticate(email, password);
//		
//		if (user == null) {
//			
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//			
//		}
//		
//		String token = jwtUtil.generateToken(user.getUserId());
//		
//		return ResponseEntity.ok(Map.of("token", token));
//		
//	}
//
//}
