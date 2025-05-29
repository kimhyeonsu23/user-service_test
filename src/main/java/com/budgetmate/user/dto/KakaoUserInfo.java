package com.budgetmate.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserInfo {
    private String id;       // 카카오 고유 ID
    private String email;    // 이메일
    private String nickname; // 프로필 닉네임
}
