package com.example.entity;

import lombok.Data;

/**
 * 管理员信息实体类
 * 用于管理员登录认证、权限控制等场景
 */
@Data
public class Admin  extends Account {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String role;
    private String avatar;

    private Integer[] ids;
    private String token;

}
