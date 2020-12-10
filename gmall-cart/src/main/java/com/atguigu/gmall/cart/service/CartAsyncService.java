package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {
    @Autowired
    private CartMapper cartMapper;

    //更新到数据库
    @Async  //异步写入Mysql
    public void updateCartToMysql(String userId, Cart cart){
        cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id",userId).eq("sku_id",cart.getSkuId()));
    }

    @Async
    public void insertCart(String userId, Cart cart) {
        int i = 1/0;
        cartMapper.insert(cart);
    }

    @Async
    public void deleteCart(String userId) {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId));
    }

    @Async
    public void deleteCartByUserIdAndSkuId( String userId, Long skuId) {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userId).eq("sku_id",skuId));
    }
}
