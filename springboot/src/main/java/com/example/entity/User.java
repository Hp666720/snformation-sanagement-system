package com.example.entity;

import lombok.Data;

/**
 * 用户信息实体类
 * 用于用户登录认证、请假申请等场景
 */
@Data
public class User extends Account {
    private Integer id;
    private String username;
    private String name;
    private String password;
    private String phone;
    private String email;
    private String role;

    private Integer[] ids;
    private String token;

}
