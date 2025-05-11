package me.khs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	@PostMapping("/createUser")
	public UserDto createUser(@RequestParam String userName, @RequestParam String password, @RequestParam String email) {
		
		UserDto userDto = new UserDto(userName, password, email);
		userService.createUser(userDto);
		return userDto;
		
	}

}
