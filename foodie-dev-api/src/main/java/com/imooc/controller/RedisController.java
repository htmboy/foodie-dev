package com.imooc.controller;




import com.imooc.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@Controller // Controller 在 SpringMVC 用得比较多，可以做页面的跳转
@ApiIgnore // 生成文档时忽略该api
@RestController // 默认的返回出去的结果是json对象
@RequestMapping("redis")
public class RedisController {

    @Autowired // 注入 redis 模板
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisOperator redisOperator;

    @GetMapping("/set")
    public Object set(String key, String value) {

        // opsForValue() 操作字符串类型
//        redisTemplate.opsForValue().set(key, value);

        redisOperator.set(key, value);
        return "ok";
    }

    @GetMapping("/get")
    public String get(String key) {

//        return (String)redisTemplate.opsForValue().get(key);
        return redisOperator.get(key);
    }

    @GetMapping("/delete")
    public Object delete(String key) {
//        redisTemplate.delete(key);
        redisOperator.del(key);
        return "ok";
    }

    /**
     * 大量的key查询 multiGet 批量查询优化
     * @param keys
     * @return
     */
    @GetMapping("/getAlot")
    public Object getAlot(String... keys) {
        List<String> result = new ArrayList<>();
        for (String k : keys) {
            result.add(redisOperator.get(k));
        }
        return "ok";
    }

    /**
     * 批量查询 pipeline
     */
    @GetMapping("/batchGet")
    public Object batchGet(String... keys) {
        List<String> keysList = Arrays.asList(keys);
        return redisOperator.batchGet(keysList);
    }
}
