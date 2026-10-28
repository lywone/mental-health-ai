package com.mental.health.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus：分页插件 + createdAt/updatedAt 自动填充
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /** 字段自动填充：insert 填 createdAt/updatedAt，update 填 updatedAt */
    @Component
    public static class FillMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
            strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
