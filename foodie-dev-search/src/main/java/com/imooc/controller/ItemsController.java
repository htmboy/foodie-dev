package com.imooc.controller;




import com.imooc.service.ItemsESService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.PagedGridResult;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController // 默认的返回出去的结果是json对象
@RequestMapping("items")
public class ItemsController {

    @Autowired
    private ItemsESService itemService;

    @GetMapping("/hello")
    public Object hello() {

        return "Hello Elasticsearch~!";
    }

    @GetMapping("/es/search")
    public IMOOCJSONResult search(
            String keywords, String sort,
            Integer page, Integer pageSize) {

        if(StringUtils.isBlank(keywords)) {
            return IMOOCJSONResult.errorMsg(null);
        }

        if(page == null) {
            page = 1;
        }

        if(pageSize == null) {
            pageSize = 20;
        }

        page --;

        PagedGridResult grid = itemService.searchItems(keywords, sort, page, pageSize);

        return IMOOCJSONResult.ok(grid);
    }

}
