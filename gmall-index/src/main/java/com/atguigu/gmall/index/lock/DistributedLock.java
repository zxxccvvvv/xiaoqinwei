package com.atguigu.gmall.index.lock;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Thread thread;

    public Boolean tryLock(String lockName, String uuid, Integer expireTime){
        String script = "if(redis.call('exists', KEYS[1])==0 or redis.call('hexists', KEYS[1],ARGV[1]) ==1)\n" +
                "then \n" +
                "\tredis.call('hincrby',KEYS[1],ARGV[1], 1)\n" +
                "\tredis.call('expire', KEYS[1],ARGV[2]);\n" +
                "\treturn 1;\n" +
                "else\n" +
                "\treturn 0;\n" +
                "end;";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expireTime.toString());
        if (!flag){
            try {
                Thread.sleep(50);
                tryLock(lockName, uuid, expireTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        renewTime(lockName, uuid, expireTime);
        return true;
    }

    public void unlock(String lockName, String uuid){
        String script = "if(redis.call('hexists',KEYS[1], ARGV[1]) == 0)\n" +
                "then\n" +
                "\treturn nil;\n" +
                "elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1)==0)\n" +
                "then\n" +
                "\tredis.call('del',KEYS[1]);\n" +
                "\treturn 1;\n" +
                "else\n" +
                "\treturn 0;\n" +
                "end;";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid);
        if (flag == null){
            throw new RuntimeException("您要释放的锁不存在，获取在尝试释放别人的锁！");
        }
        thread.interrupted();
    }

    private void renewTime(String lockName, String uuid, Integer expireTime){
        String script = "if(redis.call('hexists', KEYS[1],ARGV[1])==1)\n" +
                "then \n" +
                "\tredis.call('expire',KEYS[1],ARGV[2]);\n" +
                "\treturn 1;\n" +
                "else\n" +
                "\treturn 0;\n" +
                "end";
        thread = new Thread(() -> {
            while (true){
                try {
                    Thread.sleep(expireTime*2000/3);
                    redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expireTime.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }, "");
        thread.start();
    }



}
