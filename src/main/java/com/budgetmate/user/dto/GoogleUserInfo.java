package com.budgetmate.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleUserInfo {
    private String id;
    private String email;
    private String name;
}
