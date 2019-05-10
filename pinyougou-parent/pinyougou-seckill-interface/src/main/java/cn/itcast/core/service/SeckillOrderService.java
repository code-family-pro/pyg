package cn.itcast.core.service;

import cn.itcast.core.pojo.seckill.SeckillOrder;
import entity.PageResult;

public interface SeckillOrderService {
    PageResult search(Integer page, Integer rows, SeckillOrder seckillOrder);

    SeckillOrder findOne(Long id);
}
