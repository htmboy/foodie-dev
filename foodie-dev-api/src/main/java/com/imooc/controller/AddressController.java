package com.imooc.controller;




import com.imooc.pojo.UserAddress;
import com.imooc.pojo.bo.AddressBO;
import com.imooc.pojo.vo.NewItemsVO;
import com.imooc.service.AddressService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.MobileEmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Api(value = "地址相关", tags = {"地址相关的api接口"})
@RequestMapping("address")
@RestController // 默认的返回出去的结果是json对象
public class AddressController {

    /**
     * 用户在确认订单页面，可以针对收获地址做如下操作
     * 1、查询用户所有收货地址列表
     * 2、新增收货地址
     * 3、删除收货地址
     * 4、修改收货地址
     * 5、设置默认地址
     */

    @Autowired
    private AddressService addressService;

    @ApiOperation(value = "根据用户id查询收货地址列表", notes = "根据用户id查询收货地址列表", httpMethod = "POST")
    @PostMapping("/list")
    public IMOOCJSONResult list(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId) {
        if(StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg("");
        }

        List<UserAddress> list = addressService.queryAll(userId);
        return IMOOCJSONResult.ok(list);
    }

    @ApiOperation(value = "用户新增地址", notes = "用户新增地址", httpMethod = "POST")
    @PostMapping("/add")
     // 这里如果是对象，要用 @RequestBody 否则用 @RequestParam
    // AddressBO 里面的属性与前端传递过来的参数名一一对应
    public IMOOCJSONResult add(@RequestBody AddressBO addressBO) {

        // 判断前端发送过来的数据
        IMOOCJSONResult checkRes = checkAddress(addressBO);
        if(checkRes.getStatus() != 200) {
            return checkRes;
        }

        addressService.addNewUserAddress(addressBO);

        return IMOOCJSONResult.ok();
    }

    private IMOOCJSONResult checkAddress (AddressBO addressBO) {
        String receiver = addressBO.getReceiver();
        if (StringUtils.isBlank(receiver)) {
            return IMOOCJSONResult.errorMsg("收货人不能为空");
        }
        if(receiver.length() > 12) {
            return IMOOCJSONResult.errorMsg("收货人姓名不能太长");
        }

        String mobile = addressBO.getMobile();
        if(StringUtils.isBlank(mobile)) {
            return IMOOCJSONResult.errorMsg("收货人手机号码不能为空");
        }
        if(mobile.length() != 11) {
            return IMOOCJSONResult.errorMsg("收货人手机号长度不正确");
        }
        boolean isMobileOk = MobileEmailUtils.checkMobileIsOk(mobile);
        if(!isMobileOk) {
            return IMOOCJSONResult.errorMsg("收货人手机号码格式不正确");
        }

        String provice = addressBO.getProvince();
        String city = addressBO.getCity();
        String district = addressBO.getDistrict();
        String detail = addressBO.getDetail();

        if(StringUtils.isBlank(provice) || StringUtils.isBlank(city) ||
                StringUtils.isBlank(district) || StringUtils.isBlank(detail)) {
            return IMOOCJSONResult.errorMsg("收货地址信息不能为空");
        }
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户修改地址", notes = "用户修改地址", httpMethod = "POST")
    @PostMapping("/update")
    // 这里如果是对象，要用 @RequestBody 否则用 @RequestParam
    public IMOOCJSONResult update(@RequestBody AddressBO addressBO) {

        if(StringUtils.isBlank(addressBO.getAddressId())) {
            return IMOOCJSONResult.errorMsg("修改地址错误，addressId不能为空");
        }
        // 判断前端发送过来的数据
        IMOOCJSONResult checkRes = checkAddress(addressBO);
        if(checkRes.getStatus() != 200) {
            return checkRes;
        }

        addressService.updateUserAddress(addressBO);

        // 前端接收ok
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户删除地址", notes = "用户删除地址", httpMethod = "POST")
    @PostMapping("/delete")
    // 这里如果是对象，要用 @RequestBody 否则用 @RequestParam
    public IMOOCJSONResult delete(
            @RequestParam String userId,
            @RequestParam String addressId) {

        // 尽量不要让空的数据到达数据库
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            return IMOOCJSONResult.errorMsg("修改地址错误，userId，addressId不能为空");
        }

        addressService.deleteUserAddress(userId, addressId);

        // 前端接收ok
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户设置默认地址", notes = "用户设置默认地址", httpMethod = "POST")
    @PostMapping("/setDefalut")
    // 这里如果是对象，要用 @RequestBody 否则用 @RequestParam
    public IMOOCJSONResult setDefalut(
            @RequestParam String userId,
            @RequestParam String addressId) {

        // 尽量不要让空的数据到达数据库
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            return IMOOCJSONResult.errorMsg("");
        }

        addressService.updateUserAddressToBeDefault(userId, addressId);

        // 前端接收ok
        return IMOOCJSONResult.ok();
    }

}
