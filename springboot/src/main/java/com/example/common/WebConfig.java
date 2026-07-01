package com.example.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC Web配置类
 * 注册JWT拦截器并配置路径白名单
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    /** JWT认证拦截器 */
    @Autowired
    private JWTInterceptor jwtInterceptor;


    /**
     * 注册拦截器：拦截所有路径，排除登录/注册接口和静态资源
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        log.info("注册JWT拦截器，配置路径白名单");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 登录注册接口无需Token
                        "/admin/login",
                        "/user/login",
                        "/user/register",
                        "/user/checkUsername",
                        "/admin/checkUsername",
                        // 管理员导出/导入接口
                        "/admin/exportAdmin",
                        "/admin/importAdmin",
                        // 用户导出/导入接口
                        "/user/exportUser",
                        "/user/importUser",
                        // 静态资源文件
                        "/**/*.html",
                        "/**/*.js",
                        "/**/*.css",
                        // 文件下载接口
                        "/file/download/**",
                        "/file/upload"
                         );

    }

}
