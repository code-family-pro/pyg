package cn.itcast.core.service;

import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.Cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private PayLogDao payLogDao;

    @Override
    public void add(Order order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(order.getUserId());

        //实付金额,所有订单总金额
        long tp=0;
        //订单集合
        List<String> ids=new ArrayList<>();
        for (Cart cart : cartList) {
            //订单ID 唯一
            long id = idWorker.nextId();
            order.setOrderId(id);

            ids.add(String.valueOf(id));

            //总金额
            double totalPrice=0;
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                //订单详情表

                orderItem.setId(idWorker.nextId());
                //根据库存ID 查询库存对象
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //商品ID
                orderItem.setGoodsId(item.getGoodsId());
                //订单ID
                orderItem.setOrderId(id);
                //标题
                orderItem.setTitle(item.getTitle());
                //单价
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));

                //计算此订单的总金额
                totalPrice+=orderItem.getTotalFee().doubleValue();

                //图片
                orderItem.setPicPath(item.getImage());
                //商家ID
                orderItem.setSellerId(item.getSellerId());
                //保存订单详情表
                orderItemDao.insertSelective(orderItem);
            }

            //实付金额
            order.setPayment(new BigDecimal(totalPrice));
            //状态:未付款
            order.setStatus("1");
            //创建时间
            order.setCreateTime(new Date());
            //更新时间
            order.setUpdateTime(new Date());
            //来源
            order.setSourceType("2");
            //商家ID
            order.setSellerId(cart.getSellerID());

            tp+=order.getPayment().doubleValue()*100;
            orderDao.insertSelective(order);
        }

        //保存日志表
        PayLog payLog=new PayLog();
        //ID
        payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
        //生成时间
        payLog.setCreateTime(new Date());
        //总金额
        payLog.setTotalFee(tp);
        //用户ID
        payLog.setUserId(order.getUserId());
        //交易状态
        payLog.setTradeState("0");
        //支付类型
        payLog.setPayType("1");
        //订单集合
        payLog.setOrderList(ids.toString().replace("[","").replace("]",""));
        //保存
        payLogDao.insertSelective(payLog);
        //保存缓存一分
        redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
        //清除购物车
        //redisTemplate.boundHashOps("CART").delete(order.getUserId());
    }
}
