package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.Cart;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;


    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9003",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {

        try {

            //先获取cookie缓存
            Cookie[] cookies = request.getCookies();
            List<Cart> cartList = null;
            boolean k= false;
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    //判断cookie缓存中是否有购物车对象
                    if ("CART".equals(cookie.getName())) {
                        k=true;
                        //有  取出购物车中的数据
                        String value = cookie.getValue();
                        String decode= URLDecoder.decode(value,"utf-8");
                        cartList = JSON.parseArray(decode, Cart.class);
                        break;
                    }
                }
            }
            //如果cookie缓存中没有购物车对象就创建一个新的购物车
            if (cartList == null) {
                cartList = new ArrayList<>();
            }

            Item item = cartService.findItemById(itemId);

            //创建一个新的车子
            Cart newCart = new Cart();
            newCart.setSellerID(item.getSellerId());
            OrderItem newOrderItem = new OrderItem();
            newOrderItem.setItemId(itemId);
            newOrderItem.setNum(num);

            List<OrderItem> newOrderItemList = new ArrayList<>();
            newOrderItemList.add(newOrderItem);
            newCart.setOrderItemList(newOrderItemList);

            //判断购物车集合中是否存在新车子(商家)
            int i = cartList.indexOf(newCart);
            if (i != -1) {
                //存在
                Cart oldCart = cartList.get(i);
                List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();

                //判断购物车中是否存在该商品
                int i1 = oldOrderItemList.indexOf(newOrderItem);
                if (i1 != -1) {
                    //存在,就在原有得数量基础上加上新追加的商品数量
                    OrderItem oldOrderItem = oldOrderItemList.get(i1);
                    oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                } else {
                    //购物车中不存在该商品,就在购物车中追加该商品
                    oldOrderItemList.add(newOrderItem);
                }

            } else {
                //不存在就在购物车集合中追加新的车子
                cartList.add(newCart);
            }

            //判断用户是否登录
            String name= SecurityContextHolder.getContext().getAuthentication().getName();


            if (!"anonymousUser".equals(name)){
                //已登录
//                将合并后购物车追加到Redis中
                cartService.addCartListToRedis(cartList,name);
//                清空Cookie并回写浏览器
                if (k=true) {
                    Cookie cookie = new Cookie("CART", null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }

            }else {
                //未登录
                //如果缓存中不存在购物车对象就创建新的购物车对象
                String s=JSON.toJSONString(cartList);
                Cookie cookie = new Cookie("CART", URLEncoder.encode(s,"utf-8"));

                //设置cookie存活时间
                cookie.setMaxAge(60*60*24*7);
                //为cookie设置路径
                cookie.setPath("/");
                //返回cookie
                response.addCookie(cookie);
            }


            return new Result(true,"添加购物车成功");
        }catch (Exception e){
            return new Result(false,"添加购物车失败");
        }
    }


    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){

        //判断用户是否登录
        String name= SecurityContextHolder.getContext().getAuthentication().getName();

        //先获取cookie缓存
        Cookie[] cookies = request.getCookies();
        List<Cart> cartList = null;
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //判断cookie缓存中是否有购物车对象
                if ("CART".equals(cookie.getName())) {
                    //有  取出购物车中的数据
                    String value = cookie.getValue();
                    String decode=null;
                    try {
                        decode=URLDecoder.decode(value,"utf-8");
                        cartList = JSON.parseArray(decode, Cart.class);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        if (!"anonymousUser".equals(name)){
            //已登录
            //3:有 将此购物车合并到Redis缓存中  清空Cookie 并回写浏览器
            if (cartList!=null){
                cartService.addCartListToRedis(cartList,name);
                Cookie cookie = new Cookie("CART", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }

           //  4:查询所有购物车从Redis中
           cartList= cartService.findCartListFromRedis(name);

        }


        if (cartList!=null){
           cartList= cartService.findCartList(cartList);
        }
        return cartList;

    }
}
