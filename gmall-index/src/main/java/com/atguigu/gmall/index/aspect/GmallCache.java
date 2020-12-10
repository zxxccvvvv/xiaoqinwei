package com.atguigu.gmall.index.aspect;


import java.lang.annotation.*;

@Target({ElementType.METHOD})  //这个注解可以加在什么地方
@Retention(RetentionPolicy.RUNTIME)  //
@Documented
public @interface GmallCache {

    /*
    * 缓存key的前缀
    * 结构：模块名+':'+实例名+':'
    * 如： 首页工程三级分类缓存名
    * index:cates:
    *
    *
    * */

    String prefix() default "gmall:cache";

    /*
    * 缓存的过期时间
    *
    * */
    long timeout() default 5L;

    /*
    *
    * 为防止缓存雪崩，给缓存时间添加随机值
    * */
    int random() default 5;

    /*
    * 为了防止缓存击穿，给缓存加分布式锁
    * 这里指定分布式锁的前缀
    * 最终分布式锁名称以：lock + 方法参数
    * */

    String lock() default "lock:";

}
