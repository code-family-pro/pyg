package cn.itcast.core.service;

import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class AllOrderListServiceImpl implements AllOrderListService  {

    @Autowired
    private OrderDao orderDao;
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Order order) {
        PageHelper.startPage(pageNum,pageSize);
        OrderQuery orderQuery = new OrderQuery();
        OrderQuery.Criteria criteria = orderQuery.createCriteria();
        criteria.andSellerIdEqualTo(order.getSellerId());
        if (null != order.getOrderId() &&! "".equals(order.getOrderId().toString().trim())){
            criteria.andOrderIdEqualTo(order.getOrderId());
        }
        if (null != order.getUserId() && !"".equals(order.getUserId().trim())){
            criteria.andUserIdLike("%"+order.getUserId().trim()+"%");
        }
        Page<Order> page = (Page<Order>) orderDao.selectByExample(orderQuery);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public Order findOne(Long orderId) {
        Order order = orderDao.selectByPrimaryKey(orderId);
        return order;
    }

    @Override
    public void update(Order order) {
        order.setUpdateTime(new Date());
        orderDao.updateByPrimaryKey(order);
    }
}
