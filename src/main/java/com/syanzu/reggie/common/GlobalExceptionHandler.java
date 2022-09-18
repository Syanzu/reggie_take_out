package com.syanzu.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 *  全局异常处理器：
 *      1 底层基于代理（代理Controllers）
 *      2 通过AOP将controller的请求拦截
 */
/*          ControllerAdvice：类注解
                    位置：controller增强类（本质上是AOP的Advice通知）
                    作用：为controller类做增强

                    annotations：表示需要拦截添加了哪些注解的类（指定需要增强哪些controller）

 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})  // 指定拦截哪些类
@ResponseBody // 通过这个注解可以把数据封装成json数据返回给前端
@Slf4j
public class GlobalExceptionHandler {
    /*
            @ControllerAdvice：类注解
                位置：在controller增强类的上方，即AOP的Advice上方
                作用：对指定的controller类做增强
                annotation：参数为需要增强的controller类的注解


            @ExceptionHandler：方法注解
                位置：在异常处理的controller控制器上方
                作用：本质上就是一个Exception处理的controller方法。设置了指定Exception的处理方案，
                    功能等同于controller方法。出现异常之后，终止原始的controller方法执行，并转入当前方法的执行。
                参数：表示遇到该异常就会被拦截下来，并进行处理。
 */


    /**
     * 异常处理的方法
     * @param ex
     * @return
     */
    /*
           ExceptionHandler：方法注解，专用于异常处理的controller控制器上方
           （设置指定异常的处理方案，功能等同于controller方法，出现异常后终止原始controller执行，并转入当前方法执行）
                    参数：表示遇到该异常，就会被拦截下来，进行处理
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)  // 指定拦截哪些异常
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        // 将 重复的username 提取出来返回给前端
        if(ex.getMessage().contains("Duplicate entry")){ // 1 先判断异常是否为 "重复异常"
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 处理自定义的异常CustomException
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)  // 指定捕获的异常种类
    public R<String> exceptionHandler(CustomException ex){
        log.info(ex.getMessage());

        return R.error(ex.getMessage());
    }

}

