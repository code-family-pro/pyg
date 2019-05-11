package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.Cart;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Item findItemById(Long itemId) {
       return itemDao.selectByPrimaryKey(itemId);
    }

    @Override
    public List<Cart> findCartList(List<Cart> cartList) {

        for (Cart cart : cartList) {
            List<OrderItem> orderItemList = cart.getOrderItemList();
           /* redisTemplate.boundHashOps("CART").delete();*/
            for (OrderItem orderItem : orderItemList) {

                Item item = findItemById(orderItem.getItemId());
                orderItem.setPicPath(item.getImage());
                orderItem.setTitle(item.getTitle());
                orderItem.setPrice(item.getPrice());
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));
                cart.setSellerName(item.getSeller());
            }
        }
        return cartList;
    }

    @Override
    public void addCartListToRedis(List<Cart> newCartList, String name) {

        //1:从缓存中获取购物车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
        //2:将新购物车与缓存中的老购物车进行合并
        oldCartList = mergeCartList(newCartList,oldCartList);
        //3:将老购物车再保存到缓存中
        redisTemplate.boundHashOps("CART").put(name,oldCartList);
    }

    @Override
    public List<Cart> findCartListFromRedis(String name) {
        return (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
    }

    private List<Cart> mergeCartList(List<Cart> newCartList, List<Cart> oldCartList) {

        //判断新购物车中是否有值
        if (newCartList!=null && newCartList.size()>0){
            //有  判断老购物车中是否有值
            if (oldCartList!=null && oldCartList.size()>0){
                //有 循环遍历新车子集合
                for (Cart newCart : newCartList) {
                    //看每个车子看是否在老车子集合中存在
                    int i = oldCartList.indexOf(newCart);
                    if (i!=-1){
                        //存在 取出老车子
                        Cart oldCart = oldCartList.get(i);
                        //取出老车子中的商品集合
                        List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                        //取出新车子中的商品集合
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        //遍历新车子的商品集合
                        for (OrderItem newOrderItem : newOrderItemList) {
                            //看每个商品在老车子中是否存在
                            int i1 = oldOrderItemList.indexOf(newOrderItem);
                            if (i1!=-1){
                                //存在  就把新车子的数量跟老车子中的商品数量加到一起
                                OrderItem oldOrderItem = oldOrderItemList.get(i1);
                                oldOrderItem.setNum(newOrderItem.getNum()+oldOrderItem.getNum());
                            }else {
                                //如果不存在就往老车子中追加该商品
                                oldOrderItemList.add(newOrderItem);
                            }
                        }

                    }else {
                        //不存在,就把新车子追加到老车子集合中
                        oldCartList.add(newCart);
                    }

                }
            }else {
                //如果老购物车中没有值,就返回新购物车
                return newCartList;
            }
        }
        //返回老购物车
        return oldCartList;
    }
}
