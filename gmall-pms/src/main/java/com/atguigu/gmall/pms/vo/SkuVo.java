package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuVo extends SkuEntity {

    private List<String> images;


    private BigDecimal growBounds;
    private BigDecimal buyBounds;

    private List<Integer> work;

    // 满减活动
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    private Integer fullCount;
    private BigDecimal discount;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer addOther;

    private List<SkuAttrValueEntity> saleAttrs;


}
