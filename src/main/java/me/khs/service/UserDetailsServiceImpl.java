package me.khs.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.khs.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		//userEntity에서 userDetils를 implements를 했기 때문에 레지토리가 데이터베이스를 뒤져서 찾아온 userEntity객체 userDetails객체로도 자동 인식함.
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("이메일을 찾을 수 없습니다." + email));
		
	}

}
