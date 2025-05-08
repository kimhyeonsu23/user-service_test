package me.khs.service;

import org.springframework.stereotype.Service;

import me.khs.dto.UserDto;
import me.khs.entity.UserEntity;
import me.khs.repository.UserRepository;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository) {
		
		this.userRepository = userRepository;
		
	}
	
	public void createUser(UserDto userDto) {
		
		UserEntity user = UserEntity.builder()
				.userName(userDto.getUserName())
				.password(userDto.getPassword())
				.email(userDto.getEmail())
				.lastWeek(0)
				.currentWeek(0)
				.point(0)
				.build();
		userRepository.save(user);
		
	}

}
