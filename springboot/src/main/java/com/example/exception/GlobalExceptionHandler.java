package com.example.exception;

/**
 * @description: 类说明
 * @author: liu
 * @date: 2026/3/30 9:23
 */

import com.example.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

/**
 * 全局异常处理器
 * 统一捕获Controller层抛出的异常，转换为标准Result格式返回
 * 作用范围：com.example.controller包下的所有Controller
 */
@ControllerAdvice("com.example.controller")
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有未捕获的系统异常
     * 记录完整日志，向客户端返回通用提示（隐藏技术细节）
     * @return Result对象（code="500"，message="系统异常"）
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody // 将result对象转换成 json的格式
    public Result error(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常");
    }

    /**
     * 处理自定义业务异常
     * 将异常中的code和msg透传给前端
     * @param e 业务异常对象
     * @return 包含业务错误码和消息的Result对象
     */
    @ExceptionHandler(CustomerException.class)
    @ResponseBody // 将result对象转换成 json的格式
    public Result customerError(CustomerException e) {
        log.error("自定义错误", e);
        return Result.error(e.getCode(), e.getMsg());
    }

    /**
     * 处理参数校验异常
     * 将异常中的错误信息透传给前端
     * @param e 参数校验异常对象
     * @return 包含错误信息的Result对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(message);
    }

    /**
     * 处理数据库操作异常
     * 返回通用的错误信息
     * @param e 数据库操作异常对象
     * @return 错误信息
     */
    @ExceptionHandler(SQLException.class)
    public Result handleSQLException(SQLException e) {
        log.error("数据库异常", e);
        return Result.error("数据库操作失败，请稍后重试");
    }
}
