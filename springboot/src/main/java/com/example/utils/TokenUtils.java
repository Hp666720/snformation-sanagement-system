package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.mapper.AdminMapper;
import com.example.mapper.UserMapper;
import com.example.service.AdminService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class TokenUtils {

    @Autowired
    AdminService adminService;
    @Autowired
    private  AdminMapper adminMapper;
    @Autowired
    private  UserMapper userMapper;


    // 静态变量 原因：静态变量在类加载时就会初始化，而普通变量则需要实例化后才能初始化
    static AdminService staticAdminService;
    static UserMapper staticUserMapper;
    static AdminMapper staticAdminMapper;

    // springboot工程启动后会加载这段代码
    @PostConstruct
    public void init() {
        staticAdminService = adminService;
        staticUserMapper = userMapper;
        staticAdminMapper = adminMapper;
    }

    public static Account getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");

        if (StrUtil.isBlank(token)) {
            return null;
        }
        // 拿到token 的载荷数据
        // 根据token解析出来的userId去对应的表查询用户信息
        String audience = JWT.decode(token).getAudience().get(0);
        String[] split = audience.split("-");
        String userId = split[0];
        String role = split[1];

        Account account = new Account();
        account.setId(Integer.parseInt(userId));
        account.setRole(role);

        if ("ADMIN".equals(role)) {
            account.setUsername(staticAdminMapper.getById(Integer.parseInt(userId)).getUsername());
        } else if ("USER".equals(role)) {
            account.setUsername(staticUserMapper.selectUserById(Integer.parseInt(userId)).getUsername());
        }

        return account;
    }


    /**
     * 生成token
     */
    public static String createToken(String userId, String role, String password) {
        return JWT.create()
                .withAudience(userId + "-" + role)
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))
                .sign(Algorithm.HMAC256(password));
    }



}