package com.imooc.mapper;

import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.vo.CategoryVO;
import com.imooc.pojo.vo.NewItemsVO;
import com.imooc.pojo.vo.center.MyOrdersVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 自定义
 */
public interface OrdersMapperCustom {

    public List<MyOrdersVO> queryMyOrders(@Param("paramsMap") Map<String, Object> map);

    public int getMyOrderStatusCounts(@Param("paramsMap") Map<String, Object> map);

    public List<OrderStatus> getMyOrderTrend(@Param("paramsMap") Map<String, Object> map);
}