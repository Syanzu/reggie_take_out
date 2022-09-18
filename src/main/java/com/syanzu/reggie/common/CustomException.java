package com.syanzu.reggie.common;


/**
 * 自定义业务异常
 */
public class CustomException extends RuntimeException{

    // 提供一个构造方法，将Exception异常信息传进来（调用父类的构造器）
    public CustomException(String message){
        super(message);
    }
}
