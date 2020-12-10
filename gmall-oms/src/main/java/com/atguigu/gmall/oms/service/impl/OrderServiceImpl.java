package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.feign.GmallUmsClient;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderItemService;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemService itemService;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public OrderEntity saveOrder(OrderSubmitVo submitVo, Long userId) {
        //1、保存订单表
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setOrderSn(submitVo.getOrderToken());
        orderEntity.setCommentTime(new Date());
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        if (userEntity != null){
            orderEntity.setUsername(userEntity.getUsername());
        }
        orderEntity.setTotalAmount(submitVo.getTotalPrice());
        orderEntity.setPayAmount(submitVo.getTotalPrice());
        orderEntity.setPayType(submitVo.getPayType());
        orderEntity.setSourceType(0);
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(submitVo.getDeliveryCompany());
        UserAddressEntity address = submitVo.getAddress();
        if (address != null){
            orderEntity.setReceiverAddress(address.getAddress());
            orderEntity.setReceiverRegion(address.getRegion());
            orderEntity.setReceiverProvince(address.getProvince());
            orderEntity.setReceiverPostCode(address.getPostCode());
            orderEntity.setReceiverCity(address.getCity());
            orderEntity.setReceiverPhone(address.getPhone());
            orderEntity.setReceiverName(address.getName());
        }
        orderEntity.setDeleteStatus(0);
        orderEntity.setUseIntegration(submitVo.getBounds());
        save(orderEntity);
        Long orderId = orderEntity.getId();
        //2.保存订单详情表
        List<OrderItemVo> items = submitVo.getItems();
        List<OrderItemEntity> itemEntities = items.stream().map(item -> {
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setOrderId(orderId);
            itemEntity.setOrderSn(submitVo.getOrderToken());
            CompletableFuture<SkuEntity> skuEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
                ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
                SkuEntity skuEntity = skuEntityResponseVo.getData();
                if (skuEntity != null) {
                    itemEntity.setSkuId(skuEntity.getId());
                    itemEntity.setSkuPic(skuEntity.getDefaultImage());
                    itemEntity.setSkuPrice(skuEntity.getPrice());
                    itemEntity.setSkuQuantity(item.getCount().intValue());
                    itemEntity.setSkuName(skuEntity.getName());
                }
                return skuEntity;
            }, threadPoolExecutor);



            CompletableFuture<Void> skuAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrsBySkuId(item.getSkuId());
                List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
                itemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValueEntities));
            }, threadPoolExecutor);


            CompletableFuture<Void> spuEntityCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
                ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
                SpuEntity spuEntity = spuEntityResponseVo.getData();
                if (spuEntity != null) {
                    itemEntity.setSpuId(spuEntity.getId());
                    itemEntity.setSpuName(spuEntity.getName());
                    itemEntity.setCategoryId(spuEntity.getCategoryId());
                }
            }, threadPoolExecutor);
            CompletableFuture<Void> spuDescEntityCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
                ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
                SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
                if (spuDescEntity != null) {
                    itemEntity.setSpuPic(spuDescEntity.getDecript());
                }
            }, threadPoolExecutor);

            // 积分和品牌

           /* ResponseVo<List<ItemSaleVo>> saleResponseVo = smsClient.querySalesBySkuId(skuEntity.getId());
            List<ItemSaleVo> itemSaleVos = saleResponseVo.getData();
            if (itemSaleVos != null){
                itemEntity.setIntegrationAmount(itemSaleVos.);
            }
            */
            CompletableFuture<Void> brandEntityCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
                ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null) {
                    itemEntity.setSpuBrand(brandEntity.getName());
                }
            }, threadPoolExecutor);
            CompletableFuture.allOf(skuAttrCompletableFuture, spuEntityCompletableFuture, spuDescEntityCompletableFuture, brandEntityCompletableFuture);
            return itemEntity;
        }).collect(Collectors.toList());
        itemService.saveBatch(itemEntities);
        return orderEntity;
    }

}