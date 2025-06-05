package com.budgetmate.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String userName;

    @ElementCollection(fetch = FetchType.EAGER) // 엔티티 자체가 아니라 문자열, 정수 등의 값 객체 리스트를 하나의 테이블로 저장.
    // @ElementCollection() : JPA가 List<String>을 별도 테이블로 관리하게 함.
    // fetch = FetchType.EAGER : User조회시 roles도 즉시 함께 로딩.
    @Builder.Default
    private List<String> roles = new ArrayList<>(List.of("ROLE_USER"));

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoginType loginType = LoginType.LOCAL;  // 기본값 local

    @Column
    private String socialId;  // 소셜 로그인 ID (nullable)

    @Builder.Default
    private int lastWeek = 0;

    @Builder.Default
    private int currentWeek = 0;

    @Builder.Default
    private int point = 0;

    @Column(updatable = false) // JPA가 createAt 필드를 수정하지 않도록 설정.
    @Temporal(TemporalType.TIMESTAMP) // Date 타입을 DB의 timestamp로 매핑(날짜 + 시간 포함)
    private Date createdAt; // 각각 생성시간

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt; // 수정시간.
    
    @Column(name = "badge")
    @Builder.Default
    private int userBadge = 0; // 뱃지 상태를 처음에는 0으로 초기화.

    // Spring Security 필수 메서드 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    @PrePersist // entitiy가 처음 저장되기 전(insert전에)자동 호출됨.
    protected void onCreate() {
        this.createdAt = new Date(); // 생성일 초기화.
        this.updatedAt = new Date(); // 수정일 초기화
    }

    @PreUpdate // entitiy가 엡데이트 되기 전(update 전에)자동 호출됨.
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    public String getUserName() {
        return this.userName;
    }

}
