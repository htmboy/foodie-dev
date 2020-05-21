package com.imooc.config;

import com.imooc.service.OrderService;
import com.imooc.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 定时任务需要到 Application.java 中开启定时任务 @EnableScheduling
@Component
public class OrderJob {

    @Autowired
    private OrderService orderService;

    /**
     * 使用定时任务关闭超期未支付订单，会存有弊端
     * 1、会有时间差，程序不严谨，10：39下单，11：00检查不足1小时，12：00检查，超过1小时多39分钟，不能准时关闭订单
     * 2、不支持集群，单机没毛病，使用集群后，就会有多个定时任务，解决方案：只使用一台计算机节点，单独用来运行所有定时任务
     * 3、会对数据库全表搜索，影响数据库性能
     * 定时任务，仅仅只适用于小型轻量级项目，传统项目
     *
     * 后续课程会涉及到消息队列：MQ -> RabbitMQ, RocketMQ, Kafka, ZeroMQ...
     *      延时任务（队列）
     *      10：12分下单的，未付款（10）状态，11：12分检查，如果当前状态还是10，则直接关闭订单即可
     */

    // 对于订单未支付超过时间的，需要将其关闭。所以需要定时查询订单，判断是否到期
    // cron 表达式的网站 cron.qqe2.com
    // 0/3 * * * * ? 表示从0秒后开始，每隔3秒会执行一次
//    @Scheduled(cron = "0/3 * * * * ?")
    // 0 0 0/1 * * ? 表示从0秒后开始，每隔1天会执行一次
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void autoCloseOrder() {
        orderService.closeOrder();
        System.out.println("执行定时任务，当前时间为：" + DateUtil.getCurrentDateString(DateUtil.DATETIME_PATTERN));

    }
}
