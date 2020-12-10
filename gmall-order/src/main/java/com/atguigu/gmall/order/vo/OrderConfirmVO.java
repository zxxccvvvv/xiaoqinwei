package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @description:订单确认页的数据模型
 * @author: XQW
 * @date: 2020/11/22 10:29
 */
@Data
public class OrderConfirmVO {

    private List<UserAddressEntity> addresses;

    private List<OrderItemVo> orderItems;

    private Integer bounds;

    private String orderToken;  //防重，防止订单重复提交，造成大量锁库存

    //TODO: 发票  优惠
}
