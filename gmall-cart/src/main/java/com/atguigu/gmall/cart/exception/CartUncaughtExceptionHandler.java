package com.atguigu.gmall.cart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/20 16:24
 */
@Component
@Slf4j
public class CartUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String EXCEPTION_KEY = "cart:exception";
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {

        log.error("异步任务出现异常信息：{}, 方法：{}, 参数：{}", ex, method, params);

        //记录异步执行失败的用户Id
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(EXCEPTION_KEY);
        if (params != null && params.length != 0){
            setOps.add(params[0].toString());
        }
    }
}
