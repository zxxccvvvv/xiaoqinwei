package com.atguigu.gmall.scheduled.jobhandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/20 18:01
 */
@Component
public class CartJobHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    private static final String KEY_PREFIX = "cart:info:";

    private static final String EXCEPTION_KEY = "cart:exception";

    @XxlJob("cartJobHandler")
    public ReturnT<String> handler(String param){
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(EXCEPTION_KEY);

        //随机取出一个userId
        String userId = setOps.pop();
        //只要可以取出一个非空userId就无限循环
        while (StringUtils.isNotBlank(userId)){
            //先清空mysql, userId用户的所有购物车数据
            cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId));
            //根据userId查找redis购物车数据
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();
            if (!CollectionUtils.isEmpty(cartJsons)){
                 //最后添加到mysql中
                 cartJsons.forEach(cartJson -> {
                     Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                     cartMapper.insert(cart);
                 });
                 //去下一个
                userId = setOps.pop();
            }
        }
        return ReturnT.SUCCESS;

    }



}
