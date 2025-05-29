package com.budgetmate.user.dto;

import com.budgetmate.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SocialLoginResult {
    private final User user;
    private final boolean requiresConsent;
}
