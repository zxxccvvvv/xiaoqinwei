package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/22 12:02
 */
//订单确认页数据模型
@Data
public class OrderSubmitVo {

    private String orderToken; //防重
    private BigDecimal totalPrice; //验总价
    private UserAddressEntity address; //收货人信息
    private Integer bounds; //使用积分信息
    private Integer payType; //支付类型
    private String deliveryCompany; //配送方式
    private List<OrderItemVo> items; //送货清单

}
