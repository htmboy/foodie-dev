package com.imooc.controller;

import com.imooc.service.StuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//@Controller // Controller 在 SpringMVC 用得比较多，可以做页面的跳转
@RestController // 默认的返回出去的结果是json对象
public class StuFooController {
    @Autowired
    private StuService stuService;

    @GetMapping("/getStu")
    public Object getStu(int id) {
        return stuService.getStuInfo(id);
    }

    @PostMapping("/saveStu")
    public Object saveStu() {
        stuService.saveStu();
        return "ok";
    }

    @PostMapping("/updateStu")
    public Object updateStu(int id) {
        stuService.updateStu(id);
        return "ok";
    }

    @PostMapping("/deleteStu")
    public Object deleteStu(int id) {
        stuService.deleteStu(id);
        return "ok";
    }
}
