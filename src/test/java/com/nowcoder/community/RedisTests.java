package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
    @Autowired
    // private RedisTemplate redisTemplate;// 这里不加泛型也可以，但是会有警告
    private RedisTemplate<String, Object> redisTemplate;


    // Redis:String类型操作
    @Test
    public void testStrings() {
        // redis的key
        String redisKey = "test:count";
        // 设置值
        redisTemplate.opsForValue().set(redisKey, 1);
        // 获取值
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        // 增加值
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        // 减少值
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    // Redis:Hash类型操作
    @Test
    public void testHashes() {
        String redisKey = "test:user";
        // 设置值
        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");
        // 获取值
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    // Redis:List类型操作
    @Test
    public void testLists() {
        String redisKey = "test:ids";
        // 左侧插入
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        // 右侧插入
        redisTemplate.opsForList().rightPush(redisKey, 103);
        // 获取值个数
        System.out.println(redisTemplate.opsForList().size(redisKey));
        // 获取索引为0的值
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        // 获取索引为0到2的值
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        // 左侧弹出
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        // 右侧弹出
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
    }

    // Redis:Set类型操作
    @Test
    public void testSets() {
        String redisKey = "test:teachers";
        // 添加值
        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "诸葛亮");
        // 获取值个数
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        // 随机弹出一个值
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        // 获取所有值
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    // Redis:Sorted Set类型操作
    @Test
    public void testSortedSets() {
        String redisKey = "test:students";
        // 添加值
        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 50);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 70);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 60);
        // 获取值个数
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        // 获取某个值的分数
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "八戒"));
        // 获取某个值的排名
        System.out.println(redisTemplate.opsForZSet().rank(redisKey, "八戒"));
        // 获取某个值的排名（倒序）
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "八戒"));
        // 获取某个范围的值
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));
        // 获取某个范围的值（倒序）
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    // Redis:公共key操作
    @Test
    public void testKeys() {
        // 删除某个key
        redisTemplate.delete("test:user");
        // 判断某个key是否存在
        System.out.println(redisTemplate.hasKey("test:user"));
        // 设置某个key的过期时间
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    // Redis:多次访问同一个key
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        // 绑定key
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(redisKey);
        // 绑定key后，可以直接操作key，不用再传入key
        operations.increment();
        operations.increment();
        operations.increment();
        // 获取值
        System.out.println(operations.get());
    }

    // Redis:编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                // 启用事务
                operations.multi();
                // 执行事务
                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");
                // 获取值
                System.out.println(operations.opsForSet().members(redisKey));// []，返回空，因为事务还未提交
                // 提交事务
                return operations.exec();
            }
        });
        // 打印事务执行结果
        System.out.println(obj);// [1, 1, 1, [lisi, zhangsan, wangwu]]，返回的是一个集合，集合中的元素分别是每个操作的返回结果
    }
}
