package com.imooc.controller;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//@Controller // Controller 在 SpringMVC 用得比较多，可以做页面的跳转
@ApiIgnore // 生成文档时忽略该api
@RestController // 默认的返回出去的结果是json对象
public class HelloController {

    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    public Object hello() {
        logger.debug("debug: hello~");
        logger.info("info: hello~");
        logger.warn("warn: hello~");
        logger.error("error: hello~");
        // System.out.println("用这种输出调试信息的方式很low。应该使用log4j");
        return "Hello World!";
    }

    @GetMapping("/setSession")
    public Object setSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("userInfo", "new user");
        session.setMaxInactiveInterval(3600);
        session.getAttribute("userInfo");
        // session.removeAttribute("userInfo");
        return "ok";
    }
}
