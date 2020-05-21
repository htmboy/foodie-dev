package com.imooc.controller;


import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Api(value = "购物车接口Controller", tags = {"购物车接口相关的api"})
@RequestMapping("shopcart")
@RestController // 默认的返回出去的结果是json对象
// 购物车不存数据库，存redis
public class ShopcatController extends BaseController {

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response) {

        if(StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg("");
        }

//        System.out.println(shopcartBO);

        // 前端用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存
        // 需要判断当前购物车中包含已经存在的商品，如果存在则累加购买数量

        // “:”在redis里面起到合并子类的作用，方便用redis desktop manager观看
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        List<ShopcartBO> shopcartList = null;

        // 判断redis里面是否有历史数据
        if (StringUtils.isNotBlank(shopcartJson)) {

            // redis中已经有购物车了
            shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

            // 判断购物车中是否存在已有商品，如果有的话counts累加
            boolean isHaving = false;
            for (ShopcartBO sc : shopcartList) {
                String tmpSpecId = sc.getSpecId();
                // 判断历史数据里面的商品是否与现有的商品重合，有的话就累加
                if(tmpSpecId.equals(shopcartBO.getSpecId())) {
                    sc.setBuyCounts(sc.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            // 没有的重合的话就把商品添加到购物车列表中
            if(!isHaving) {
                shopcartList.add(shopcartBO);
            }
        } else {

            // redis 中没有购物车历史数据
            shopcartList = new ArrayList<>();

            // 直接添加到购物车
            shopcartList.add(shopcartBO);
        }
        redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));

        return IMOOCJSONResult.ok();

    }

    @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
    @PostMapping("/del")
    public IMOOCJSONResult del(
            @RequestParam String userId,
            @RequestBody String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response) {

        if(StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)) {
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }

        // 用户在页面删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除redis中的商品
        // 查询redis购物车历史数据
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        // 判断redis是否有购物车历史数据
        if(StringUtils.isNotBlank(shopcartJson)) {

            // 将购物车历史数据josn转化为对查询redis是否有购物车历史数据象
            List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

            // 遍历查询是否有重合的商品
            for (ShopcartBO sc : shopcartList) {
                String tmpSpecId = sc.getSpecId();

                // 判断是否有重合的商品
                if (tmpSpecId.equals(itemSpecId)) {

                    // 删除商品
                    shopcartList.remove(sc);
                    break;
                }
            }

            // 覆盖现有的redis 数据
            redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));
        }

        return IMOOCJSONResult.ok();

    }
}
