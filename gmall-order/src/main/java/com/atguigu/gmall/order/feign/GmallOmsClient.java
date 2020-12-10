package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/23 20:05
 */
@FeignClient("service-oms")
public interface GmallOmsClient extends GmallOmsApi {
}
