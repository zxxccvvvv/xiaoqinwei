package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter<String> bloomFilter;

    @Around("@annotation(GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //1. 获取目标方法GmallCache注解对象
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //2. 获取目标方法对象
        Method method = signature.getMethod();
        //3. 获取目标方法上的注解
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        //4. 获取gmallCache中的前缀属性
        String prefix = gmallCache.prefix();
        //5. 获取目标方法的返回值类型
        Class<?> returnType = method.getReturnType();
        //6. 获取目标方法的参数列表
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        //组装成缓存key
        String key = prefix + args;
        //去bloomfilter查询有没有数据
        boolean flag = bloomFilter.contains(key);
        if (!flag){
            return null;
        }
        //再去查询缓存，缓存中有直接反序列化后，直接返回
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json, returnType);
        }
        String lock = gmallCache.lock();

            //防止缓存击穿，可以添加分布式锁
        RLock fairLock = redissonClient.getFairLock(lock + args);
        fairLock.lock();
        try {
            //防止其他请求已经缓存了数据，再次查询缓存, 如果有了就直接返回
            String json2 = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(json2)){
                return JSON.parseArray(json2, returnType);
            }
            //执行目标方法， 获取数据库中的数据
            Object result = joinPoint.proceed(joinPoint.getArgs());

            //放入缓存， 如果result为null, 为了防止缓存穿透，依然放入缓存，但缓存时间极短
            if (result == null){

            }else {
                long timeout =  gmallCache.timeout()  + new Random().nextInt(gmallCache.random());
                //放入缓存
                redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout, TimeUnit.MINUTES);
            }
            //如果存在则返回结果
            return result;
        } finally {
            //释放分布式锁
            fairLock.unlock();
        }
    }
}
