package com.imooc.controller;




import com.imooc.enums.YesOrNo;
import com.imooc.mapper.CategoryMapper;
import com.imooc.pojo.Carousel;
import com.imooc.pojo.Category;
import com.imooc.pojo.vo.CategoryVO;
import com.imooc.pojo.vo.NewItemsVO;
import com.imooc.service.CarouselService;
import com.imooc.service.CategoryService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Api(value = "首页", tags = {"首页展示的相关接口"})
@RestController // 默认的返回出去的结果是json对象
@RequestMapping("index")
public class IndexController {

    @Autowired
    private CarouselService carouselService;

    @Autowired
    private CategoryService categoryService;

    @Autowired // redis 改造
    private RedisOperator redisOperator;

    @ApiOperation(value = "获取首页轮播图列表", notes = "获取首页轮播图列表", httpMethod = "GET")
    @GetMapping("/carousel")
    public IMOOCJSONResult carousel() {

        List<Carousel> list = new ArrayList<>();
        // 判断是否在redis里面
        String carouselstr = redisOperator.get("carousel");
        if (StringUtils.isBlank(carouselstr)) {

            // 数据库中取出轮播图
            list = carouselService.queryAll(YesOrNo.YES.type);

            // 将list转换为json字符串，放入redis
            redisOperator.set("carousel", JsonUtils.objectToJson(list));

        } else {

            list = JsonUtils.jsonToList(carouselstr, Carousel.class);
        }



        return IMOOCJSONResult.ok(list);
    }

    /**
     * 如何更新 redis 里面的缓存？
     * 1. 后台运营系统，一旦广告（轮播图）发生更改，就可以删除缓存，然后重置
     * 2. 定时重置，比如每天凌晨三点重置
     * 3. 每个轮播图都有可能是一个广告，每个广告都会有一个过期时间，过期了，再重置
     *
     */


    @ApiOperation(value = "获取商品分类（一级分类）", notes = "获取商品分类（一级分类）", httpMethod = "GET")
    @GetMapping("/cats")
    public IMOOCJSONResult cats() {
        List<Category> list = categoryService.queryAllRootLevelCat();
        return IMOOCJSONResult.ok(list);
    }


    @ApiOperation(value = "获取商品子分类", notes = "获取商品子分类", httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")
    public IMOOCJSONResult subCat(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId) {
        if(rootCatId == null) {
            return IMOOCJSONResult.errorMsg("");
        }

        // 创建CategoryVO list
         List<CategoryVO> list = new ArrayList<>();
        String catsStr = redisOperator.get("subCat:" + rootCatId);


        if (StringUtils.isBlank(catsStr)) {
            list = categoryService.getSubCatList(rootCatId);

            /**
             * 查询的key在redis中不存在
             * 对应的id在数据库也不存在
             * 此时被非法用户进行攻击，大量的请求会直接打在db上
             * 造成宕机，从而影响整个系统，
             * 这种现象称之为缓存穿透。
             * 解决方案：把空的数据也缓存起来，比如空字符串，空对象，空数组或list
             */
            // 防止恶意刷数据库，缓存穿透
//            if (list != null && list.size() > 0) {
//                redisOperator.set("subCat:" + rootCatId, JsonUtils.objectToJson(list));
//            }else {
//                redisOperator.set("subCat:" + rootCatId, JsonUtils.objectToJson(list));
//            }
            // 下面效果与上面一样
            redisOperator.set("subCat:" + rootCatId, JsonUtils.objectToJson(list));
        } else {
            list = categoryService.getSubCatList(rootCatId);
        }

        return IMOOCJSONResult.ok(list);
    }


    @ApiOperation(value = "查询每个一级分类下的最新6条商品数据", notes = "查询每个一级分类下的最新6条商品数据", httpMethod = "GET")
    @GetMapping("/sixNewItems/{rootCatId}")
    public IMOOCJSONResult sixNewItems(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId) {
        if(rootCatId == null) {
            return IMOOCJSONResult.errorMsg("分类不存在");
        }

        List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
        return IMOOCJSONResult.ok(list);
    }
}
