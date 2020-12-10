package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    @Override
    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //1.根据skuId查询sku信息
        CompletableFuture<SkuEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new RuntimeException("商品信息不存在");
            }
            itemVo.setSkuId(skuEntity.getId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            itemVo.setWeight(skuEntity.getWeight());
            return skuEntity;
        }, threadPoolExecutor);

        //2.根据sku中的分类Id查询三级分类
        CompletableFuture<Void> catesCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryAllLvlCategoriesByCid3(skuId);
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        //3.根据sku中的品牌id查询品牌信息
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);
        //4.根据sku中的spuId查询spu信息
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

            ResponseVo<SpuEntity> spuResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);


        //5.根据skuId查询sku图片信息
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> imagesResponseVo = pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = imagesResponseVo.getData();
            itemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);


        //6.根据skuId查询sku的营销信息
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> saleResponseVo = smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = saleResponseVo.getData();
            itemVo.setSales(itemSaleVos);

        }, threadPoolExecutor);

        //7.根据skuid查询库存信息
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSukBySukId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);


        //8.根据spuId查询spu下的所有sku的销售属性
        CompletableFuture<Void> saleAttrsCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = pmsClient.querySaleAttrValuesBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);


        //9.根据skuId查询当前sku的销售属性
        CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = pmsClient.querySaleAttrsBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> map = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(map);
            }
        }, threadPoolExecutor);


        // 10.根据spuId查询spu下所有sku和销售属性组合的映射关系
        CompletableFuture<Void> mappingCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> stringResponseVo = pmsClient.querySaleAttrValuesMappingSkuId(skuEntity.getSpuId());
            String json = stringResponseVo.getData();
            itemVo.setSkusJson(json);
        }, threadPoolExecutor);

        // 11.根据spuId查询商品描述信息
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescResponseVo.getData();
            if (spuDescEntity != null) {
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);

        // 12.根据cid、skuId、spuId查询组及组下的规格参数值
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<GroupVo>> groupResponseVo = pmsClient.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(skuEntity.getCatagoryId(), skuEntity.getSpuId(), skuId);
            List<GroupVo> groupVos = groupResponseVo.getData();
            itemVo.setGroups(groupVos);
        }, threadPoolExecutor);

        CompletableFuture.allOf(catesCompletableFuture, brandCompletableFuture, spuCompletableFuture,
                imageCompletableFuture, salesCompletableFuture, wareCompletableFuture, saleAttrsCompletableFuture,
                saleAttrCompletableFuture, mappingCompletableFuture, descCompletableFuture, groupCompletableFuture
        ).join();
        return itemVo;
    }
}
