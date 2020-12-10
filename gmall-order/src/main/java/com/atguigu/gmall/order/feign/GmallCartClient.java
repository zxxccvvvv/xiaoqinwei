package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/22 10:38
 */
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
