package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SpuDescService spuDescService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );


        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(Long categoryId, PageParamVo pageParamVo) {
        //封装查询条件
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0){
            wrapper.eq("category_id", categoryId);
        }
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t->t.like("name", key).or().like("id", key));
        }
        return new PageResultVo(page(pageParamVo.getPage(), wrapper));
    }

    //@Transactional(propagation = Propagation.REQUIRED)
    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        System.out.println(spuAttrValueService);
        /// 1.保存spu相关
        // 1.1. 保存spu基本信息 spu_info
        Long spuId = saveSpu(spuVo);
        // 1.2. 保存spu的描述信息 spu_info_desc
        spuDescService.saveSpuDesc(spuVo, spuId);
        // 1.3. 保存spu的规格参数信息
        saveBaseAttr(spuVo, spuId);
        // 2. 保存sku相关信息
        List<SkuVo> skuVos = spuVo.getSkus();
        if (CollectionUtils.isEmpty(skuVos)){
            return;
        }
        // 2.1. 保存sku基本信息
        skuVos.forEach(skuVo -> {
            //保存基本信息
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo, skuEntity);
            //品牌和分类的id需要从spuInfo中获取
            skuEntity.setBrandId(spuVo.getBrandId());
            skuEntity.setCatagoryId(spuVo.getCategoryId());
            //获取图片列表
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)){
                //设置第一张图片为默认图片
                skuEntity.setDefaultImage(skuEntity.getDefaultImage() ==
                        null ? images.get(0) : skuEntity.getDefaultImage());
            }
            skuEntity.setSpuId(spuId);
            skuMapper.insert(skuEntity);
            //获取skuId
            Long skuId = skuEntity.getId();
            // 2.2. 保存sku图片信息
            if (!CollectionUtils.isEmpty(images)){
                String defaultImage = images.get(0);
                List<SkuImagesEntity> skuImageses = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setSort(0);
                    skuImagesEntity.setUrl(image);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImageses);
            }
            // 2.3. 保存sku的规格参数（销售属性）
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            saleAttrs.forEach(saleAttr -> {
                saleAttr.setSort(0);
                saleAttr.setSkuId(skuId);
            });
            skuAttrValueService.saveBatch(saleAttrs);
            // 3. 保存营销相关信息，需要远程调用gmall-sms
            // 3.1. 积分优惠
            // 3.2. 满减优惠
            // 3.3. 数量折扣
            SkuSaleVo skuSaleVo = new SkuSaleVo();


            BeanUtils.copyProperties(skuVo,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            smsClient.saveSkuSaleInfo(skuSaleVo);

           //int i = 1/0;

            rabbitTemplate.convertAndSend("PMS_SPU_EXCHANGE", "item.insert", spuId);


        });

    }



    private Long saveSpu(SpuVo spuVo) {
        spuVo.setPublishStatus(1); //默认上架
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime()); //新增时间和修改时间一致
        this.save(spuVo);

        return spuVo.getId();
    }

    private void saveBaseAttr(SpuVo spuVo, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                spuAttrValueVo.setSpuId(spuId);
                spuAttrValueVo.setSort(0);
                return spuAttrValueVo;
            }).collect(Collectors.toList());
            spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }

}