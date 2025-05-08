package me.khs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
public class UserEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	
	@Column(name = "user_name", length = 50, nullable = false)
	private String userName;
	
	@Column(name = "password", length = 50, nullable = false)
	private String password;
	
	@Column(name = "email", length = 255, nullable = false)
	private String email;
	
	@Column(name = "last_week")
	int lastWeek = 0;
	
	@Column(name = "current_week")
	int currentWeek = 0;
	
	@Column(name = "point")
	int point = 0;
	
	
}
