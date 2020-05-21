package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}) // 排除 redis 存放session的密码登录功
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

/**
 * 报错：netty的问题
 *
 * ERROR SpringApplication Application run failed
 *  org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'elasticsearchClient' defined in class path resource [org/springframework/boot/autoconfigure/data/elasticsearch/ElasticsearchAutoConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.elasticsearch.client.transport.TransportClient]: Factory method 'elasticsearchClient' threw exception; nested exception is java.lang.IllegalStateException: availableProcessors is already set to [4], rejecting [4]
 * 	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:627)
 * 	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:456)
 * 	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1321)
 * 	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1160)
 * 	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:555)
 * 	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:515)
 * 	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:320)
 * 	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:222)
 * 	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:318)
 * 	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:199)
 * 	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:843)
 * 	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:877)
 * 	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:549)
 * 	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:142)
 * 	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:775)
 * 	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:397)
 * 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:316)
 * 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1260)
 * 	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1248)
 * 	at com.imooc.Application.main(Application.java:14)
 *
 * 	设置
 * 	es.set.netty.runtime.available.processors=false
 */