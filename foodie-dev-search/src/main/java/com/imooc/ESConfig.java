package com.imooc;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 解决因netty的报错
 * 设置
 * es.set.netty.runtime.available.processors=false
 */
@Configuration
public class ESConfig {

    /**
     * 解决netty引起的issue
     */
    @PostConstruct
    void init() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }
}
