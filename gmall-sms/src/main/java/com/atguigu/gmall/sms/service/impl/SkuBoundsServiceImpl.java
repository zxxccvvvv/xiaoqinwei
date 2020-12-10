package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionMapper reductionMapper;
    @Autowired
    private SkuLadderMapper ladderMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public void saveSkuSaleInfo(SkuSaleVo skuSaleVo) {
        // 3.1. 积分优惠
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo,skuBoundsEntity);
        //数据库报讯的是整数0-15，页面绑定是0000-1111
        List<Integer> work = skuSaleVo.getWork();
        if (!CollectionUtils.isEmpty(work)){
            skuBoundsEntity.setWork(work.get(0)*8 + work.get(1)*4 + work.get(2)*2 + work.get(3));
        }
        save(skuBoundsEntity);

        // 3.2. 满减优惠
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        reductionMapper.insert(skuFullReductionEntity);

        // 3.3. 数量折扣

        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
        ladderMapper.insert(skuLadderEntity);

    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(Long skuId) {
        List<ItemSaleVo> itemSaleVos = new ArrayList<>();
        //根据skuId查询积分优惠信息
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null){
            ItemSaleVo boundSaleVo = new ItemSaleVo();
            boundSaleVo.setType("积分");
            boundSaleVo.setDesc("送" + skuBoundsEntity.getGrowBounds() + "成长积分，送" + skuBoundsEntity.getBuyBounds() + "购物积分");
            itemSaleVos.add(boundSaleVo);
        }

        // 根据skuId查询满减优惠
        SkuFullReductionEntity fullReductionEntity = this.reductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (fullReductionEntity != null) {
            ItemSaleVo reductionSaleVo = new ItemSaleVo();
            reductionSaleVo.setType("满减");
            reductionSaleVo.setDesc("满" + fullReductionEntity.getFullPrice() + "减" + fullReductionEntity.getReducePrice());
            itemSaleVos.add(reductionSaleVo);
        }
        //根据skuId查询打折优惠信息
        SkuLadderEntity ladderEntity = this.ladderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (ladderEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("满" + ladderEntity.getFullCount() + "件打" + ladderEntity.getDiscount().divide(new BigDecimal(10)) +"折");
            itemSaleVos.add(itemSaleVo);
        }
        return itemSaleVos;
    }

}