package com.imooc.controller;




import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisOperator;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller // Controller 包含页面
public class SSOController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_TICKET = "redis_user_ticket";
    public static final String REDIS_TMP_TICKET = "redis_tmp_ticket";
    public static final String COOKIE_USER_TICKET = "cookie_user_ticket";

    @GetMapping("/login")
    public Object login(String returnUrl, Model model, HttpServletRequest request, HttpServletResponse response) {

        model.addAttribute("returnUrl", returnUrl);

        // 1，获取userTicket门票，如果cookie中能够获取到，证明用户登录过，此时签发一个一次性的临时票据并且回跳
        String userTicket = getCookie(request, COOKIE_USER_TICKET);

        boolean isVerified = verifyUserTicket(userTicket);
        if (isVerified) {
            String tmpTicket = createTmpTicket();
            return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
        }

        // 用户从没登录过，第一次进入则登录sso登录页面
        return "login";
    }

    /**
     * 校验CAS全局用户门票
     * @return
     */
    private boolean verifyUserTicket(String userTicket) {

        // 1，验证CAS门票不能为空
        if (StringUtils.isBlank(userTicket)) {
            return false;
        }

        // 2，验证CAS门票是否有效
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return false;
        }

        // 3， 验证门票对应的user会话是否存在
        String userRedis = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return false;
        }
        return true;
    }

    /**
     * CAS的统一登录接口
     * 目的：
     *     1，登录后创建用户的全局会话  -> uniqueToken
     *     2，创建用户全局门票，用以表示在CAS端是否登录  -> userTicket
     *     3，创建用户的临时票据，用于回跳回传  -> tmpTicket
     *
     *     用户口令 + 用户id
     *
     *     REDIS_USER_TICKET:userTicket 的值是 用户id 起到关联作用
     *
     * userTicket：用于表示用户在CAS端一个登录状态：已经登录
     * tmpTicket：用于颁发给用户进行一次性的验证票据，有时效性
     *
     *
     * @return
     */
    @PostMapping("/doLogin")
    public String doLogin(String username,
                          String password,
                          String returnUrl,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) {

        // 添加变量到model
        model.addAttribute("returnUrl", returnUrl);

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            model.addAttribute("errmsg", "用户名或密码不能为空！");
            return "login";
        }

        // 1. 实现登录
        Users userResult = null;
        try {
            userResult = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(userResult == null) {
            model.addAttribute("errmsg", "用户名或密码不正确！");
            return "login";
        }

        // 2，创建用户会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult, usersVO);
        usersVO.setUserUniqueToken(uniqueToken);

        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(), JsonUtils.objectToJson(usersVO));

        // 3，生成ticket门票，全局门票，代表用户在CAS端登录过
        String userTicket = UUID.randomUUID().toString().trim();

        // 3.1， 用户全局门票需要放入CAS端的cookie中
        setCookie(COOKIE_USER_TICKET, userTicket, response);

        // 4，userTicket 关联用户id，并且放入到redis中，代表这个用户有门票了，可以在各个景区游玩
        // userTicket 用来查询 用户id
        redisOperator.set(REDIS_USER_TICKET + ":" + userTicket, userResult.getId());

        // 5，生成临时票据，回跳到调用端网站，是由CAS端所签发的一个一次性的临时ticket
        String tmpTicket = createTmpTicket();

        // 将临时ticket带过去，凭临时ticket查询userTicket
        return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;

    }

    @PostMapping("/verifyTmpTicket")
    @ResponseBody
    public IMOOCJSONResult verifyTmpTicket(String tmpTickey,
                                           HttpServletRequest request,
                                           HttpServletResponse response){
        // 使用一次性临时票据来验证用户是否登录，如果登录过，把用户会话返回给站点
        // 使用完毕后，需要销毁临时票据
        String tmpTicketValue = redisOperator.get(REDIS_TMP_TICKET + ":" + tmpTickey);
        if (StringUtils.isBlank(tmpTicketValue)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常！");
        }

        // 0，如果临时票据ok，则需要销毁，并且拿到CAS端cookie中的全局userTicket，以此再次获取用户会话
        String md5Ticket = null;
        try {
            md5Ticket = MD5Utils.getMD5Str(tmpTickey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!tmpTicketValue.equals(md5Ticket)){
            return IMOOCJSONResult.errorUserTicket("用户票据异常！");
        } else {
            // 销毁临时票据
            redisOperator.del(REDIS_TMP_TICKET + ":" + tmpTickey);
        }

        // 1，验证并且获取用户的userId
        String userTicket = getCookie(request, COOKIE_USER_TICKET);
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常！");
        }

        // 2，验证门票对应的user会话是否存在
        String userRedis = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常！");
        }


        return IMOOCJSONResult.ok(JsonUtils.jsonToPojo(userRedis, UsersVO.class));
    }

    @PostMapping("/logout")
    @ResponseBody
    public IMOOCJSONResult logout(String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response){

        // 1，获取CAS中的用户门票
        String userTicket = getCookie(request, COOKIE_USER_TICKET);

        // 2，清楚userTicket票据，redis/cookie
        deleteCookie(COOKIE_USER_TICKET, response);
        redisOperator.del(REDIS_USER_TICKET + ":" + userTicket);

        // 3，清楚用户全局会话（分布式会话）
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        return IMOOCJSONResult.ok();
    }

    private String createTmpTicket() {
        String tmpTicket = UUID.randomUUID().toString().trim();
        try {
            // 设置临时票据
            redisOperator.set(REDIS_TMP_TICKET + ":" + tmpTicket, MD5Utils.getMD5Str(tmpTicket), 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpTicket;
    }

    private void setCookie(String key, String val, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, val);
        // 需要设置cookie的作用域
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getCookie(HttpServletRequest request, String key) {

        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || StringUtils.isBlank(key)) {
            return null;
        }

        String cookieValue = null;
        for (int i = 0; i < cookieList.length; i++) {
            if(cookieList[i].getName().equals(key)) {
                cookieValue = cookieList[i].getValue();
                break;
            }
        }
        return cookieValue;
    }

    private void deleteCookie(String key, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        // 需要设置cookie的作用域
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }
}
