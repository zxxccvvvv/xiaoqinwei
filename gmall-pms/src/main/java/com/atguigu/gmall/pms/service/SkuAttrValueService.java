package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:50
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySearchAttrValueBySkuIdAndCid(Long skuId, Long cid);

    List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId);

    public String querySaleAttrValuesMappingSkuId(Long spuId);
}

