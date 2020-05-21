package com.imooc.config;

import com.imooc.controller.interceptor.UserTokenInterceptor;
import com.imooc.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// implements WebMvcConfigurer 为静态资源提供发布服务
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    // 实现静态资源的映射，设置之前需要bean
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/**")
                // 访问地址是：localhost:8088/foodie/faces/200301ACGK1G14ZC/face-200301ACGK1G14ZC.jpg
                .addResourceLocations("file:images/") // 映射本地静态资源
                // 静态资源映射会使 Swagger2 失效，不能访问 http://localhost:8088/doc.html 需要为 Swagger2 重新配置
                .addResourceLocations("classpath:/META-INF/resources/");
    }


    @Bean
    public UserTokenInterceptor userTokenInterceptor() {
        return new UserTokenInterceptor();
    }

    /**
     * 注册拦截器。注册之前，需要bean
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截 /hello 路由
        registry.addInterceptor(userTokenInterceptor()).addPathPatterns("/hello") // 添加拦截的路由
                .addPathPatterns("/shopcart/add").addPathPatterns("/shopcart/del")
                .addPathPatterns("/address/list").addPathPatterns("/address/add")
                .addPathPatterns("/address/update").addPathPatterns("/address/setDefault")
                .addPathPatterns("/address/delete").addPathPatterns("/orders/*")
                .addPathPatterns("/center/*").addPathPatterns("/userInfo/*")
                .addPathPatterns("/myorders/*").addPathPatterns("/mycomments/*")
                // 剔除拦截的路由
                .excludePathPatterns("/myorders/deliver").excludePathPatterns("/orders/notifyMerchantOrderPaid");

        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
