package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.lock.DistributedLock;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private RedissonClient redissonClient;

    public static final String KEY_PREFIX = "index:cates:";

    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategory(0L);
        return listResponseVo.getData();
    }

    @Override
    @GmallCache(prefix = KEY_PREFIX, timeout = 129600L, random = 14400, lock = "lock:cates:")
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSub(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }


    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSub2(Long pid) {
        //从缓存中获取
        String cacheCategories = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(cacheCategories)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
            return categoryEntities;
        }
        RLock lock = redissonClient.getLock("lock" + pid);
        lock.lock();

        try {
            // 加锁过程中可能已经有其他线程把数据放入缓存，再去检查缓存
            String cacheCategories1 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
            if (StringUtils.isNotBlank(cacheCategories1)){
                List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories1, CategoryEntity.class);
                return categoryEntities;
            }
            // 没有命中，执行业务远程调用 获取数据，最后放入缓存
            ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesWithSub(pid);
            List<CategoryEntity> categoryEntities = listResponseVo.getData();
            //
            if (CollectionUtils.isEmpty(categoryEntities)){
                //防止缓存穿透， 即使数据为null,也放入缓存
                redisTemplate.opsForValue().set(KEY_PREFIX+ pid,JSON.toJSONString(categoryEntities), 1, TimeUnit.MINUTES );
            }else {
                // 为了防止缓存雪崩，给缓存时间添加随机值
                redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities),2160 + new Random().nextInt(900), TimeUnit.HOURS);
            }
            return categoryEntities;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSub1(Long pid) {
        //从缓存中获取
        String cacheCategories = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(cacheCategories)){
            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
            return categoryEntities;
        }
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesWithSub(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        //
        if (CollectionUtils.isEmpty(categoryEntities)){
            //防止缓存穿透， 即使数据为null,也放入缓存
            redisTemplate.opsForValue().set(KEY_PREFIX+ pid,JSON.toJSONString(categoryEntities), 1, TimeUnit.MINUTES );
        }else {
            //
            redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities),2160 + new Random().nextInt(900), TimeUnit.HOURS);
        }
        return categoryEntities;
    }

    @Override
    public void testlock() {
        String uuid = UUID.randomUUID().toString();
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        //获取锁失败
        if (!flag) {
            try {
                Thread.sleep(50);
                testlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //redisTemplate.expire("lock", 3, TimeUnit.SECONDS);
            //获取锁成功后，之星业务代码， 最后释放锁
            String numString = redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)) {
                return;
            }
            int num = Integer.parseInt(numString);
            redisTemplate.opsForValue().set("num", String.valueOf(++num));

            //释放锁。为了防止误删，删除之前需要判断是不是自己的锁
            String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1])else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
            /*if (StringUtils.equals(uuid, redisTemplate.opsForValue().get("lock"))) {
                redisTemplate.delete("lock");
            }*/

        }
    }

    @Override
    public void testlock1() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = distributedLock.tryLock("lock", uuid, 30);
        if (lock){
            //redisTemplate.expire("lock", 3, TimeUnit.SECONDS);
            //获取锁成功后，之星业务代码， 最后释放锁
            String numString = redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)) {
                return;
            }
            int num = Integer.parseInt(numString);
            redisTemplate.opsForValue().set("num", String.valueOf(++num));

           /* try {
                TimeUnit.SECONDS.sleep(180);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        testsub("lock", uuid);
        distributedLock.unlock("lock", uuid);
    }

    public void testsub(String lockname, String uuid){
        distributedLock.tryLock(lockname, uuid, 30);
        System.out.println("测试可重入锁");
        distributedLock.unlock(lockname, uuid);

    }

    public void testWrite(){
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(10, TimeUnit.SECONDS);

        System.out.println("模仿了写操作。。。。。");

        //TODO:释放锁

    }

    public void testRead(){
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(10, TimeUnit.SECONDS);
        System.out.println("模仿了读操作");

        //TODO:释放锁
    }

    public void latch() throws InterruptedException {
        RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
        latch.trySetCount(6);
        latch.await();
    }

    public void countdown(){
        RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
        System.out.println("出来一名同学");
        latch.countDown();
    }






}
