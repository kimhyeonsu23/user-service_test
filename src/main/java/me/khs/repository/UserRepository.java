package me.khs.repository;

import me.khs.entity.UserEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
	
	Optional<UserEntity> findByEmailAndPassword(String email, String password);
	Optional<UserEntity> findByEmail(String email);
	
}
