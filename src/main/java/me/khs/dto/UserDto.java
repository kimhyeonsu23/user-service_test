package me.khs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class UserDto {
	
	private Long userId;
	private String userName;
	private String password;
	private String email;
	private int lastWeek;
	private int currentWeek;
	private int point;
	
	public UserDto(String userName, String password, String email) { // 회원가입
		
		this.userName = userName;
		this.password = password;
		this.email = email;
		
	}
	
	public UserDto(String email, String password) {
		
		this.email = email;
		this.password = password;
		
	}
	
	public UserDto() {}

}
