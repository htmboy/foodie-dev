package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}) // 排除 redis 存放session的密码登录功能
// 扫描 mybatis 通用 mapper 所在的包
@MapperScan(basePackages = "com.imooc.mapper")
// 扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"}) // 默认扫描 com.imooc
// 开启定时任务
@EnableScheduling
// 开启redis 作为 spring session 的存放
@EnableRedisHttpSession
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
