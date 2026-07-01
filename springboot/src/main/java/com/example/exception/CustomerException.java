package com.example.exception;

import lombok.Data;

/**
 * 自定义业务异常类
 * 继承RuntimeException，用于封装业务层面的错误（如账号不存在、权限不足等）
 * 由GlobalExceptionHandler统一捕获并返回标准格式的错误响应
 */
@Data
public class CustomerException extends RuntimeException {

    private String code;

    private String msg;

    /**
     * 创建包含错误码和消息的业务异常
     * @param code 错误状态码
     * @param msg  错误消息
     */
    public CustomerException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 创建仅包含消息的业务异常（默认code="500"）
     * @param msg 错误消息
     */
    public CustomerException(String msg) {
        this.code = "500";
        this.msg = msg;
    }

    /** 无参构造（反序列化使用） */
    public CustomerException() {}

}
