package com.syanzu.reggie.common;

/**
 * 基于ThreadLocal封装的工具类：用于保存和获取当前用户的ID
 *          ThreadLocal：并不是Thread，而是Thread的局部变量
 */
public class BaseContext {   // 作用范围是单个线程范围内，多个线程之间不会混淆
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();  // 代表当前线程中的局部变量threadLocal


    /**
     * 设置值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }


    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
