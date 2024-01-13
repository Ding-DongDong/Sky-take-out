package com.sky.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
public class SpringDataRedisTest {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void testRdisTemplate(){
        System.out.println(redisTemplate);
        //操作字符串
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //操作哈希
        HashOperations hashOperations = redisTemplate.opsForHash();
        ListOperations listOperations = redisTemplate.opsForList();
        SetOperations setOperations = redisTemplate.opsForSet();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
    }

    /**
     * 操作字符串数据
     */
    @Test
    public void testString(){
        //set get setex setnx
        redisTemplate.opsForValue().set("city", "boy");
        String city = (String)redisTemplate.opsForValue().get("city");
        System.out.println(city);
        redisTemplate.opsForValue().set("code", "123456", 3, TimeUnit.MINUTES);
        redisTemplate.opsForValue().setIfAbsent("lock", "1");
        redisTemplate.opsForValue().setIfAbsent("lock", "2");

    }

    /**
     * 操作哈希的数据
     */
    @Test
    public void testHash(){
        //Hset hget hdel hkeys hvals
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put("100", "name", "jimmy");
        hashOperations.put("100", "age", "20");
        String name = (String) hashOperations.get("100", "name");
        System.out.println(name);
        Set keys = hashOperations.keys(100);
        System.out.println(keys);
        List values = hashOperations.values("100");
        System.out.println(values);
        hashOperations.delete("100", "age");
    }

    /**
     * 操作列表数据
     */
    @Test
    public void testList(){
        //lpush lrange rpop llen
        ListOperations listOperations = redisTemplate.opsForList();
        listOperations.leftPushAll("mylist", "a","b","c");
        listOperations.leftPush("mylist", "d");
        List mylist = listOperations.range("mylist", 0, -1);
        System.out.println(mylist);
        listOperations.rightPop("mylist");
        Long size = listOperations.size("mylist");
        System.out.println(size);
    }
    /**
     * 集合类型操作
     */
    public void testSet(){
        //sadd smembers scard sinter srem
        SetOperations setOperations = redisTemplate.opsForSet();
        setOperations.add("set1", "a","b","c","d");
        setOperations.add("set2", "a","b","x","y");
        Set members = setOperations.members("set1");
        System.out.println(members);
        Long size = setOperations.size("set1");
        System.out.println(size);
        Set intersect = setOperations.intersect("set1", "set2");
        System.out.println(intersect);
        Set union = setOperations.union("set1", "set2");
        System.out.println(union);
        setOperations.remove("set1", "a","b");

    }
}
