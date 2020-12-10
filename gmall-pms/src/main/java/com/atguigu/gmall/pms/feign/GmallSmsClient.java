package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author 肖
 * @Date: new Date()
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
