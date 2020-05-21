package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.UserBO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.StuService;
import com.imooc.service.UserService;
import com.imooc.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "注册登陆", tags = {"用于注册登陆的相关接口"})
//@Controller // Controller 在 SpringMVC 用得比较多，可以做页面的跳转
@RestController // 默认的返回出去的结果是json对象
@RequestMapping("passport") // 路由
public class PassportController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET") // 在方法内部显示
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam /*代表请求参数，而不是路径参数*/ String username) {
        // 1.判断用户名不能为空
        if(StringUtils.isBlank(username)) {
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }

        // 2，查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if(isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }

        // 3，请求成功
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户名注册", notes = "用户名注册", httpMethod = "POST")
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO, HttpServletRequest request, HttpServletResponse response) {

        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPwd = userBO.getConfirmPassword();

        // 0 判断用户名和密码必须不为空
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(confirmPwd)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1 查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if(isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }

        // 2 密码长度不能少于6位
        if(password.length() < 6) {
            return IMOOCJSONResult.errorMsg("密码长度不能少于6");
        }

        // 3 判断两次秘密是否一致
        if (!password.equals(confirmPwd)) {
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }

        // 4 实现注册
        Users userResult = userService.createUser(userBO);

        // 把不必要的内容忽略掉
        // userResult = setNullProperty(userResult);

        // 实现用户的redis会话
        UsersVO usersVO = conventUserVO(userResult);

        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVO),true);

        // 同步购物车数据
        synchShopcartData(userResult.getId(), request, response);

        return IMOOCJSONResult.ok();
    }



    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO, HttpServletRequest request, HttpServletResponse response) {

        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 0 判断用户名和密码必须不为空
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 1 密码长度不能少于6位
        if(password.length() < 6) {
            return IMOOCJSONResult.errorMsg("密码长度不能少于6");
        }

        String MD5Password = "";
        try {
            MD5Password = MD5Utils.getMD5Str(password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2 实现登陆
        Users userResult = userService.queryUserForLogin(username, MD5Password);

        if(userResult == null) {
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }
        userResult = setNullProperty(userResult);

        // 实现用户的redis会话
        UsersVO usersVO = conventUserVO(userResult);

        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVO),true);

        // 同步购物车数据
        synchShopcartData(userResult.getId(), request, response);

        return IMOOCJSONResult.ok(userResult);
    }

    /**
     * 注册登录成功后，同步cookie和redis中的购物车数据
     */
    private void synchShopcartData(String userId, HttpServletRequest request, HttpServletResponse response) {
        /**
         * 1，redis中无数据，如果cookie中的购物车为空，这个时候不做任何处理
         *     如果cookie中的购物车不为空，此时直接放入redis中
         * 2，redis中有数据，如果cookie中的购物车为空，那么直接把redis的购物车覆盖本地cookie
         *     如果cookie中的购物车不为空，如果cookie中的某个商品在redis中存在，则以cookie为主，
         *     删除redis中的，把cookie中的商品直接覆盖redis中（参考京东）
         * 3，同步到redis中去了以后，覆盖本地cookie购物车的数据，保证本地购物车的数据是同步最新的
         *
         */

        // 从redis中获取购物车
        String shopcartJsonRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        // 从cookie中获取购物车
        String shopcatStrCookie = CookieUtils.getCookieValue(request, FOODIE_SHOPCART, true);

        // 判断redis为空
        if (StringUtils.isBlank(shopcartJsonRedis)) {

            // redis为空，cookie不为空，直接把cookie中的数据放入redis
            if (StringUtils.isNotBlank(shopcatStrCookie)) {

                redisOperator.set(FOODIE_SHOPCART + ":" + userId, shopcatStrCookie);
            }
        } else {

            // redis不为空，cookie不为空，合并cookie和redis中购物车的商品数据（同一商品则覆盖redis）
            if (StringUtils.isNotBlank(shopcatStrCookie)) {

                /**
                 * 1，已经存在的，把cookie中对应的数量，覆盖redis（参考京东）
                 * 2，该项商品标记为待删除，统一放入一个待删除的list
                 * 3，从cookie中清理所有的待删除list
                 * 4，合并redis和cookie中的数据
                 * 5，更新到redis和cookie中
                 */

                List<ShopcartBO> shopcartListRedis = JsonUtils.jsonToList(shopcartJsonRedis, ShopcartBO.class);
                List<ShopcartBO> shopcartListCookie = JsonUtils.jsonToList(shopcatStrCookie, ShopcartBO.class);

                // 定义一个待删除的list
                List<ShopcartBO> pendingDeleteList = new ArrayList<>();

                // 遍历redis里面的商品
                for (ShopcartBO redisShopcart : shopcartListRedis) {

                    // 拿到放在redis里面的商品的id
                    String redisSpecId = redisShopcart.getSpecId();

                    // 遍历cookie里面的商品
                    for (ShopcartBO cookieShopcart : shopcartListCookie) {

                        // 获取cookie里面商品的id
                        String cookieSpecId = cookieShopcart.getSpecId();

                        // 判断cookie里面的商品和redis里面的商品是否有重复
                        if (redisSpecId.equals(cookieSpecId)) {

                            // 覆盖购买的数量，不累加，参考京东
                            redisShopcart.setBuyCounts(cookieShopcart.getBuyCounts());

                            // 把cookieshopcart放入待删除列表，用于最后的删除与合并
                            pendingDeleteList.add(cookieShopcart);
                        }
                    }

                }

                // 从现有cookie中删除对应的覆盖过的商品数据
                shopcartListCookie.removeAll(pendingDeleteList);

                // 合并两个list
                shopcartListRedis.addAll(shopcartListCookie);

                // 更新到redis和cookie
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartListRedis), true);
                redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartListRedis));

            } else {
                // redis不为空，cookie为空，直接把redis覆盖cookie
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, shopcartJsonRedis, true);
            }
        }
    }

    private Users setNullProperty(Users userResult) {

        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId, HttpServletRequest request, HttpServletResponse response) {

        // 清除用户的相关信息的cookie
        CookieUtils.deleteCookie(request, response, "user");

        // 用户退出登录，需要清空购物车
        CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART);

        // 清除用户分布式会话数据
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        return IMOOCJSONResult.ok();
    }
}
