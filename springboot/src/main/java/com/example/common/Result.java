package com.example.common;

import lombok.Data;

/**
 * 统一API响应结果封装类
 * 标准格式：{ code: 状态码, message: 提示信息, data: 业务数据 }
 */
@Data
public class Result {

    /** 业务状态码："200"成功，"500"及其他为失败 */
    private String code;

    /** 响应消息描述 */
    private String message;

    /** 响应数据载荷 */
    private Object data;

    /** 私有构造，强制使用静态工厂方法创建 */
    private Result(){}

    /**
     * 创建成功响应（完整版）
     * @param code    状态码（通常"200"）
     * @param message 成功消息
     * @param data    业务数据
     */
    public static Result success(String code, String message, Object data){
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应（简化版）
     * 自动设置code="200"，message="请求成功"
     * @param data 业务数据
     */
    public static Result success( Object data){
        Result result = new Result();
        result.setCode("200");
        result.setMessage("请求成功");
        result.setData(data);
        return result;
    }

    /**
     * 创建错误响应（完整版）
     * @param code    错误码
     * @param message 错误消息
     */
    public static Result error(String code, String message){
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 创建错误响应（简化版）
     * 自动设置code="500"
     * @param message 错误消息
     */
    public static Result error( String message){
        Result result = new Result();
        result.setCode("500");
        result.setMessage(message);
        return result;
    }
}
