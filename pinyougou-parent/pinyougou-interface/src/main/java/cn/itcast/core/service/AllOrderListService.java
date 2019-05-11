package cn.itcast.core.service;

import cn.itcast.core.pojo.order.Order;
import entity.PageResult;

public interface AllOrderListService {
    PageResult search(Integer pageNum, Integer pageSize, Order order);

    Order findOne(Long orderId);

    void update(Order order);
}
