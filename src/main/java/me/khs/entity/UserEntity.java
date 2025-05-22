package me.khs.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	
	@Column(name = "user_name", length = 50, nullable = false)
	private String userName;
	
	@Column(name = "password", length = 255, nullable = false)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	// json 변환기 jackson에게 알려주는 설정, 받을 때만 쓰게 함.
	private String password;
	
	@Column(name = "email", unique = true, length = 255, nullable = false)
	private String email;
	
	@Builder.Default
	@Column(name = "last_week")
	int lastWeek = 0;
	
	@Builder.Default
	@Column(name = "current_week")
	int currentWeek = 0;
	
	@Builder.Default
	@Column(name = "point")
	int point = 0;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@Builder.Default
	private List<String> roles = new ArrayList<> (List.of("ROLE_USER"));
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// ? : wildcard / 정확한 타입을 지정하지 않고도 이 타입 혹은 그 자식 클래스를 의미 (grantedAuthority타입이거나 그 자식 클래스)
		return roles.stream().map(SimpleGrantedAuthority::new).toList();
		
	}
	
	@Override public String getUsername() {return email;}
	@Override public boolean isAccountNonExpired() {return true;}
	// 계정이 만료되진 않았는가?
	@Override public boolean isAccountNonLocked() {return true;}
	//계정이 잠겨있진 않은가?
	@Override public boolean isCredentialsNonExpired() {return true;}
	//비밀번호가 만료되지 않았는가?
	@Override public boolean isEnabled() {return true;}
	// 
	
	
	/*
	@Builder
	public UserEntity (Long userId, String userName, String password, String email, int lastWeek, int currentWeek, int point) {
		
		this.userId = userId;
		this.userName = userName;
		this.password = password;
		this.email = email;
		this.lastWeek = lastWeek;
		this.currentWeek = currentWeek;
		this.point = point;
		
	}*/
	
}
