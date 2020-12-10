package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import sun.plugin2.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_SAVE_QUEUE", durable = "true"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message) {
        ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkuBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)){
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();
                //设置sku相关信息到goods中
                goods.setSkuId(skuEntity.getId());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setPrice(skuEntity.getPrice().doubleValue());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                //spu中的创建时间
                ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
                SpuEntity spuEntity = spuEntityResponseVo.getData();
                if (spuEntity != null) {
                    goods.setCreateTime(spuEntity.getCreateTime());
                }
                //查询库存信息
                ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSukBySukId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)){
                    //只要有任何一个仓库有货就说明有货
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                            wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));

                    //销量集合,三个仓库的销量总和
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a+b).get());
                }

                //查询品牌
                ResponseVo<BrandEntity> brandResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandResponseVo.getData();
                if (brandEntity != null){
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }

                //查询分类
                ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null){
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());

                }
                //查询规格参数

                //查询销售类型的参数
                List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                ResponseVo<List<SkuAttrValueEntity>> skuAttrResponseVo = pmsClient.querySearchAttrValueBySkuIdAndCid(skuEntity.getId(), skuEntity.getCatagoryId());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }

                //查询基本类型的参数
                ResponseVo<List<SpuAttrValueEntity>> spuAttrResponseVo = pmsClient.querySearchSpuAttrValueBySpuIdAndCid(skuEntity.getSpuId(), skuEntity.getCatagoryId());
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrResponseVo.getData();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                    searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));

                }
                goods.setSearchAttrs(searchAttrValues);

                return goods;
            }).collect(Collectors.toList());

            goodsRepository.saveAll(goodsList);
        }
    }
}
