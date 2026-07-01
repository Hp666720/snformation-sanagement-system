package com.example.entity;

import lombok.Data;

/**
 * 管理员信息
 */
@Data
public class Account {

    private Integer id;
    private String username;
    private String name;
    private String password;
    private String phone;
    private String email;
    private String role;
    private String avatar;

    private Integer[] ids;

}
