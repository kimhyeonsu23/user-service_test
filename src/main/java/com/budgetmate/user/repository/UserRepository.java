package com.budgetmate.user.repository;

import com.budgetmate.user.entity.LoginType;
import com.budgetmate.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // 소셜 로그인용 조회
    Optional<User> findBySocialIdAndLoginType(String socialId, LoginType loginType);
}

