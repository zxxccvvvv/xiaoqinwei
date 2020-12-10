package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/22 11:56
 */
//订单确认页的商品详情模型
@Data
public class OrderItemVo {

    private Long skuId;
    private String defaultImage;
    private String title;
    private List<SkuAttrValueEntity> saleAttrs; //销售属性
    private BigDecimal price; //加入购物车是的价格
    private BigDecimal count;
    private Boolean store = false;
    private List<ItemSaleVo> sales;  //营销信息
    private Integer weight;

}
