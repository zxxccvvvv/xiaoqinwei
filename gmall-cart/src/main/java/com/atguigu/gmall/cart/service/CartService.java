package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    //cart中有skuId, count
    public void addCart(Cart cart) {
        //获取登录信息，如果userId不为空，就以userId作为key,如果为空则以userKey作为key
        String userId = getUserId();
        //通过外层Key获取内层map结构
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        //判断购物车是否有此商品， 如果有则更新数量，没有则添加到购物车
        if (hashOps.hasKey(skuId)){
            //存在，则更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            //写redis
            hashOps.put(skuId, JSON.toJSONString(cart));
            //写mysql
            cartAsyncService.updateCartToMysql(userId, cart);

        }else {
            //不存在保存到数据库
            cart.setUserId(userId);
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null){
                return;
            }
            cart.setDefaultImage(skuEntity.getDefaultImage());
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());

            //查询库存信息
            ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSukBySukId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            //销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrsBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            //营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> sales = salesResponseVo.getData();
            cart.setSales(JSON.toJSONString(sales));

            cart.setCheck(true);

            //添加到数据库
            cartAsyncService.insertCart(userId.toLowerCase(),cart);
            //添加缓存价格
            redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        //写redis
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() == null){
            return userInfo.getUserKey();
        }
        return userInfo.getUserId().toString();
    }

    public Cart queryCartBySkuId(Long skuId) {
        String userId = getUserId();

        //获取redis中的map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (hashOps.hasKey(skuId.toString())){
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new CartException("此用户不存在这条购物车记录");
    }


    public List<Cart> queryCarts() {
        //1.获取userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();

        //2.查询未登录的购物车信息
        BoundHashOperations<String, Object, Object> unLoginHashOps = redisTemplate.boundHashOps(KEY_PREFIX + userKey);
        List<Object> unLoginCartsJson = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(unLoginCartsJson)){
            unLoginCarts = unLoginCartsJson.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        //3.获取userId, 并判断userId是否为空，为空则直接返回未登录的购物车
        Long userId = userInfo.getUserId();
        if (userId == null){
            return unLoginCarts;
        }
        //4.获取登录状态的购物车内层map
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        //5.获取未登录的购物车合并到登录的购物车的内层map中
        if (!CollectionUtils.isEmpty(unLoginCarts)){
            unLoginCarts.forEach(cart -> {
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount();
                //如果登陆状态的购物车中有该记录则更新数量
                if (loginHashOps.hasKey(skuId)){
                    //如果登陆状态的购物车中有该记录则更新数量
                    String cartJson = loginHashOps.get(skuId).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    this.cartAsyncService.updateCartToMysql(userId.toString(), cart);
                }else {
                    //如果登陆状态的购物车中没有该记录，则新增一条记录
                    cart.setUserId(userId.toString());
                    cartAsyncService.insertCart(userId.toString(), cart);
                }
                //更新到redis
                loginHashOps.put(skuId, JSON.toJSONString(cart));

            });
            //6.删除未登录状态的购物车并返回
            redisTemplate.delete(KEY_PREFIX + userKey);
            cartAsyncService.deleteCart(userKey);
        }


        //7.查询登陆状态的购物车并返回
        List<Object> loginCartJson = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJson)){
            return loginCartJson.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        return null;
    }

    public void updateNum(Cart cart) {
        String userId = getUserId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(cart.getSkuId().toString())){
            String  cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            BigDecimal count = cart.getCount();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
            cartAsyncService.updateCartToMysql(userId,cart);
            return;
        }
        throw new CartException("该用户的购物车不包含该条记录");
    }

    public void deleteCart(Long skuId) {
        String userId = getUserId();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (hashOps.hasKey(skuId.toString())){
            hashOps.delete(skuId.toString());
            cartAsyncService.deleteCartByUserIdAndSkuId(userId, skuId);
            return;
        }
        throw new CartException("该用户购物车中不存在该商品");
    }

    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> cartJsons = hashOps.values();
        if (CollectionUtils.isEmpty(cartJsons)){
            throw new CartException("您没有选中购物车记录");
        }
        return cartJsons.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class))
                .filter(Cart::getCheck)
                .collect(Collectors.toList());
    }

    public void updateStatus(Cart cart) {
        String skuId = cart.getSkuId().toString();
        Boolean check = cart.getCheck();

        String userId = getUserId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        String cartJson = hashOps.get(skuId).toString();
        cart = JSON.parseObject(cartJson, Cart.class);

        if (cart == null){
            return;
        }
        cart.setCheck(check);
        //更新到redis
        hashOps.put(skuId, JSON.toJSONString(cart));
        //更新到mysql
        cartMapper.updateById(cart);
    }
}
