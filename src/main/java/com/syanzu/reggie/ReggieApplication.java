package com.syanzu.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j  // 可以直接使用变量log
@SpringBootApplication
@ServletComponentScan // 有这个注解扫描过滤器，过滤器才会生效
@EnableTransactionManagement // 开启事务管理功能
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("项目启动成功");   // 代表可以直接输出info级别的日志
    }
}
