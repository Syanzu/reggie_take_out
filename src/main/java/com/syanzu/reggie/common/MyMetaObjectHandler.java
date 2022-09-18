package com.syanzu.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义的元数据对象处理器
 */
@Component   // 类注解：设置该类为Spring管理的bean
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {  // 实现MetaObjectHandler元数据处理器
    /**
     * 插入操作的时候自动填充：执行insert的时候执行
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {   // metaObject：元数据，封装了当前employee对象
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());


        // 设置自动填充
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        // 通过工具类BaseContext，将用户id取出来
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * 更新操作的时候自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());

        // 设置自动填充
        metaObject.setValue("updateTime", LocalDateTime.now());

        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
