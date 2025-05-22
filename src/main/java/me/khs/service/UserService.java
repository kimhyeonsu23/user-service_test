package me.khs.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import me.khs.dto.LoginRequest;
import me.khs.dto.SignupRequest;
import me.khs.dto.UserDto;
import me.khs.entity.UserEntity;
import me.khs.repository.UserRepository;
import me.khs.security.JwtTokenProvider;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
		
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		
	}
	
	public void createUser(SignupRequest signupRequest) {
		
		signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
				
		UserEntity user = UserEntity.builder()
				.userName(signupRequest.getUserName())
				.password(signupRequest.getPassword())
				.email(signupRequest.getEmail())
				.currentWeek(0)
				.lastWeek(0)
				.point(0)
				.build();
		userRepository.save(user);
		
	}
	
	public String login(LoginRequest request) {
		UserEntity user = userRepository.findByEmail(request.getEmail())
						.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
						
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
			// 이 코드는 내부에서 이렇게 작동함. BCrypt.checkpw(rawPassword, encodedPassword)
			// 이때 encodedPassword가 암호화된 문자열이어야 디코딩을 할 수 있음. 디코디응ㄹ 할 수 없다면 예외를 터뜨리거나 false를 리턴함.
			throw new RuntimeException("비밀번호가 일치하지 않습니다.");
		
	return jwtTokenProvider.createToken(user.getUserId(),user.getEmail(), user.getRoles());
	}
	
}
