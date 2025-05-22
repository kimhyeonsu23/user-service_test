package me.khs.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
	
	public SignupRequest() {}
	
	private String email;
	private String password;
	private String userName;

}
