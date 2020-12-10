package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/24 0:08
 */
public interface GmallOmsApi {

    @GetMapping("oms/order/submit/{userId}")
    public ResponseVo<OrderEntity> saveOrder(@RequestBody OrderSubmitVo submitVo, @PathVariable Long userId);
}
