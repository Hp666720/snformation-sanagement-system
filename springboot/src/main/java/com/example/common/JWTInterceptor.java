package com.example.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomerException;
import com.example.mapper.AdminMapper;
import com.example.mapper.UserMapper;
import com.example.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证拦截器
 * 在请求到达Controller前验证token的有效性（存在性、签名、角色合法性）
 */
@Component
@Slf4j
public class JWTInterceptor implements HandlerInterceptor {

    @Autowired
    AdminService adminService;
    /** 管理员数据访问对象 */
    @Autowired
    private AdminMapper adminMapper;
    /** 用户数据访问对象 */
    @Autowired
    private UserMapper userMapper;


    /**
     * 请求预处理：JWT Token认证核心逻辑
     * 验证流程：提取Token → 解析用户信息 → 校验存在性 → 验证签名 → 放行/拒绝
     * @return true表示认证通过；抛出CustomerException表示认证失败
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("JWT拦截器处理请求: {} {}", request.getMethod(), request.getRequestURI());
        // 优先从Header获取Token，其次从Query Parameter获取
        String token = request.getHeader("token");
        if (token == null) {
            token = request.getParameter("token");
        }
        if (StrUtil.isBlank(token)) {
            log.warn("请求未携带Token: {}", request.getRequestURI());
            throw new CustomerException("无权限操作");
        }
        try {
            // 解析token载荷：格式为 "userId-role"
            String s = JWT.decode(token).getAudience().get(0);
            String[] split = s.split("-");
            String userId = split[0];
            String role = split[1];

            // 校验用户是否存在且角色合法
            if ("ADMIN".equals(role)) {
                Admin admin = adminMapper.getById(Integer.parseInt(userId));
                if (admin == null) {
                    log.warn("Token验证失败：管理员不存在, userId={}", userId);
                    throw new CustomerException("无权限操作");
                }
            } else if ("USER".equals(role)) {
                User user = userMapper.selectUserById(Integer.parseInt(userId));
                if (user == null) {
                    log.warn("Token验证失败：用户不存在, userId={}", userId);
                    throw new CustomerException("无权限操作");
                }
            } else {
                log.warn("Token验证失败：非法角色, role={}", role);
                throw new CustomerException("非法角色");
            }

            // 使用数据库中的密码验证Token签名
            JWTVerifier build = JWT.require(Algorithm.HMAC256(getPasswordByRole(role, userId))).build();
            build.verify(token);

            log.debug("Token验证成功: userId={}, role={}", userId, role);
        } catch (CustomerException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token验证异常", e);
            throw new CustomerException("无权限操作");
        }

        return true;

    }

    /**
     * 根据角色和用户ID查询密码（用于签名验证）
     * @param role   用户角色
     * @param userId 用户ID
     * @return 用户密码字符串
     */
    private String getPasswordByRole(String role, String userId) {
        if ("ADMIN".equals(role)) {
            Admin admin = adminMapper.getById(Integer.parseInt(userId));
            return admin.getPassword();
        } else {
            User user = userMapper.selectUserById(Integer.parseInt(userId));
            return user.getPassword();
        }
    }
}
