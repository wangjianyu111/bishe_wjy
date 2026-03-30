package com.gdplatform.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private UserProfile user;
    private List<MenuNode> menus;
    private List<String> permissions;
}
