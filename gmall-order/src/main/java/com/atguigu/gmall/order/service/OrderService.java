package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


/**
 * @description:
 * @author: XQW
 * @date: 2020/11/22 10:10
 */
@Service
public class OrderService {

    @Autowired
    private GmallOmsClient omsClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallCartClient cartClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "order:token:";


    public OrderConfirmVO confirm() {
        OrderConfirmVO confirmVO = new OrderConfirmVO();

        //获取登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        //获取用户选中的购物车
        CompletableFuture<List<Cart>> cartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<List<Cart>> cartsResponseVo = cartClient.queryCheckedCartsByUserId(userId);
            List<Cart> carts = cartsResponseVo.getData();

            if (CollectionUtils.isEmpty(carts)) {
                throw new OrderException("您没有选中的购物车记录");
            }
            return carts;
        }, threadPoolExecutor);

        //把购物车记录转换成订单详情记录：skuId count
        CompletableFuture<Void> itemCompletableFuture = cartCompletableFuture.thenAcceptAsync(carts -> {
             List<OrderItemVo> itemVos = carts.stream().map(cart -> {
                OrderItemVo itemVo = new OrderItemVo();
                itemVo.setCount(cart.getCount());
                CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                    //获取sku信息,并设置OrderItemVo
                    ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
                    SkuEntity skuEntity = skuEntityResponseVo.getData();
                    if (skuEntity != null) {
                        itemVo.setSkuId(skuEntity.getId());
                        itemVo.setPrice(skuEntity.getPrice());
                        itemVo.setDefaultImage(skuEntity.getDefaultImage());
                        itemVo.setTitle(skuEntity.getTitle());
                        itemVo.setWeight(skuEntity.getWeight());
                    }
                }, threadPoolExecutor);
                CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
                    //获取库存信息，并设置给OrderItemVo
                    ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSukBySukId(cart.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                    }
                }, threadPoolExecutor);

                //获取销售树形，并设置给OrderItemVo
                CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {

                    ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrsBySkuId(cart.getSkuId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
                    itemVo.setSaleAttrs(skuAttrValueEntities);
                }, threadPoolExecutor);

                //获取营销信息并保存到OrderItemVo
                CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<ItemSaleVo>> SaleResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
                    List<ItemSaleVo> itemSaleVos = SaleResponseVo.getData();
                    itemVo.setSales(itemSaleVos);
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuCompletableFuture, saleAttrCompletableFuture, saleCompletableFuture, storeCompletableFuture).join();
                return itemVo;
            }).collect(Collectors.toList());
            confirmVO.setOrderItems(itemVos);
        }, threadPoolExecutor);

        //获取用户的收货地址列表
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<UserAddressEntity>> addressesResponseVo = umsClient.queryAddressesByUserId(userId);
            List<UserAddressEntity> addressEntities = addressesResponseVo.getData();
            confirmVO.setAddresses(addressEntities);
        }, threadPoolExecutor);

        //根据用户id查询用户信息(购买积分)
        CompletableFuture<Void> boundsCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(userId);
            UserEntity userEntity = userEntityResponseVo.getData();
            if (userEntity != null) {
                confirmVO.setBounds(userEntity.getIntegration());
            }
        }, threadPoolExecutor);

        //生成orderToken
        CompletableFuture<Void> tokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getTimeId();
            confirmVO.setOrderToken(orderToken);
            redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);
        }, threadPoolExecutor);
        CompletableFuture.allOf(itemCompletableFuture, addressCompletableFuture, boundsCompletableFuture, tokenCompletableFuture).join();
        return confirmVO;
    }

    public String submit(OrderSubmitVo submitVo) {
        //1.防重
        String orderToken = submitVo.getOrderToken();
        if (StringUtils.isEmpty(orderToken)){
            throw new OrderException("请求不合法！");
        }
        String script = "if(redis.call('exists', KEYS[1]) == 1) then return redis.call('del', KEYS[1]) else return 0 end";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken));
        if (!flag){
            throw new OrderException("页面已过期或者您已经提交");
        }
        //2.验总价
        List<OrderItemVo> items = submitVo.getItems();
        if (CollectionUtils.isEmpty(items)){
            throw new OrderException("您没有选中的购物车记录");
        }
        //数据库的实时价格
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(item.getCount());
            }
            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();
        BigDecimal totalPrice = submitVo.getTotalPrice();
        if (totalPrice != currentTotalPrice){
            throw new OrderException("页面已过期，请刷新后重试");
        }

        //3.验库存
        List<SkuLockVo> lockVos = items.stream().map(item -> {
            SkuLockVo lockVo = new SkuLockVo();
            lockVo.setSkuId(item.getSkuId());
            lockVo.setCount(item.getCount().intValue());
            return lockVo;

        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> listResponseVo = wmsClient.checkAndLock(lockVos, orderToken);
        List<SkuLockVo> skuLockVos = listResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuLockVos)){
            throw new OrderException(JSON.toJSONString(lockVos));
        }

        //4.创建订单
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        try {
            omsClient.saveOrder(submitVo, userId);
            //订单创建成功后，定时关单
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE","order.ttl",orderToken);
        } catch (AmqpException e) {
            //异步标记为无效订单
            e.printStackTrace();
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.invalid", orderToken);
            throw new OrderException("服务器错误，创建订单失败");
            
        }

        //5.删除购物车
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        map.put("skuIds", JSON.toJSONString(skuIds));
        //异步删除购物车
        rabbitTemplate.convertAndSend("order_exchange","cart.delete",map);

        return orderToken;
     }
}
