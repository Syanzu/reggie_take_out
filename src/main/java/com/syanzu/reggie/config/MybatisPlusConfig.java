package com.syanzu.reggie.config;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置MP的分页插件
 */
@Configuration
public class MybatisPlusConfig {


    // 通过 拦截器 的方式将插件加载进来
    @Bean // 加Bean注解，让Spring来管理
    public MybatisPlusInterceptor mybatisPlusInterceptor(){

        // 1 创建 MybatisPlusInterceptor 拦截器对象
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();

        // 通过 addInnerInterceptor 这个方法添加 分页拦截器 PaginationInnerInterceptor
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
