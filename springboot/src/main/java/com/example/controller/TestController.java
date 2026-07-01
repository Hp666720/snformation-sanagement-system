package com.example.controller;

import com.example.common.Result;
import com.example.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 类说明
 * @author: liu
 * @date: 2026/3/30 8:52
 */
@RestController
public class TestController {

    @Autowired
    private AdminService adminService;

    @RequestMapping("/hello")
    public String hello(){
        return "hello";
    }

    @RequestMapping("/testResult")
    public Result testResult(){
        return Result.success("测试result");
    }
    @RequestMapping("/testExc")
    public Result testExc(){
        return Result.success(1/0);
    }


}
