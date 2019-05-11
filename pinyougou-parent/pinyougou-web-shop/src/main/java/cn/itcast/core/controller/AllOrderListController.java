package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.AllOrderListService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/allOrderList")
public class AllOrderListController {

    @Reference
    private AllOrderListService allOrderListService;

    @RequestMapping("/search")
    public PageResult search(Integer pageNum, Integer pageSize, @RequestBody Order order){
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setSellerId(sellerId);
        return allOrderListService.search(pageNum,pageSize,order);
    }

    @RequestMapping("/findOne")
    public Order findOne(Long orderId){
        //System.out.println(orderId);
        return allOrderListService.findOne(orderId);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody Order order){
        try {
            allOrderListService.update(order);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
}
