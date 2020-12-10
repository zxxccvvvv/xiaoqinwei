package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/22 10:38
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
