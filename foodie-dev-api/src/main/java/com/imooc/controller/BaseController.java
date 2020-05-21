package com.imooc.controller;

import com.imooc.pojo.Orders;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.center.MyOrdersService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.RedisOperator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.UUID;

@Controller
public class BaseController {

    @Autowired
    private RedisOperator redisOperator;

    public static final String FOODIE_SHOPCART = "shopcart";
    public static final Integer COMMENT_PAGE_SIZE = 10;
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    // 支付中心的调用地址
    String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";

    // 支付成功后的回调地址
    String payReturnUrl = "http://localhost:8088/foodie-dev-api/orders/notifyMerchantOrderPaid";

    // 用户上传头像的位置
    // 这个路径写在这里有个小问题，如果有测试环境，预发布环境，等等多种环境，这个地址就要写很多份
    // 这只是一个属性的配置，一旦有很
    // 多个属性的话那就需要来回的进行注释，这显然很不好。有没更好的方式来管理呢
    // 解决方法：通过资源配置
    public static final String IMAGE_USER_FACE_LOCALTION = "images" + File.separator + "foodie" + File.separator + "faces";


    @Autowired
    public MyOrdersService myOrdersService;

    public IMOOCJSONResult checkUserOrder(String userId, String orderId) {
        Orders order = myOrdersService.queryMyOrder(userId, orderId);
        if(order == null) {
            return IMOOCJSONResult.errorMsg("订单不存在");
        }
        // 调用此方法需要获取data，要传order
        return IMOOCJSONResult.ok(order);
    }

    public UsersVO conventUserVO(Users user){
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USER_TOKEN + ":" + user.getId(), uniqueToken);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 将token 保存到用户的cookie里面去
        usersVO.setUserUniqueToken(uniqueToken);

        return usersVO;
    }
}
