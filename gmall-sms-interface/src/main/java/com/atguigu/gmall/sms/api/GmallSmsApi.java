package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author 肖
 * @Date: new Date()
 */

public interface GmallSmsApi {

    //根据skuId查询优惠信息
    @GetMapping("sms/skubounds/sales/sku/{skuId}")
    public ResponseVo<List<ItemSaleVo>> querySalesBySkuId(@PathVariable("skuId") Long skuId);

    //保存sku优惠信息
    @PostMapping("/sms/skubounds/skusale/save")
    ResponseVo<Object> saveSkuSaleInfo(@RequestBody SkuSaleVo skuSaleVo);
}
