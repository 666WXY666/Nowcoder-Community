package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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

    // Redis:HyperLogLog类型操作
    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog() {
        String redisKey = "test:hll:01";

        // 添加10w数据
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        // 添加10w数据
        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        // 获取统计的结果
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    // 将3组数据合并，再统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        // 合并
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        // 获取统计的结果
        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    // Redis:Bitmap类型操作
    // 统计一组数据的布尔值
    @Test
    public void testBitmap() {
        String redisKey = "test:bm:01";

        // 记录
        // setBit key offset value
        // 将key对应的value的二进制的offset位设置为value
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 3, true);
        redisTemplate.opsForValue().setBit(redisKey, 5, true);

        // 查询
        // getBit key offset
        // 获取key对应的value的二进制的offset位的值
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));

        // 统计
        // bitCount key [start] [end]
        // 统计key对应的value的二进制的start到end位中1的个数
        System.out.println(redisTemplate.execute((RedisCallback<Object>) con -> con.stringCommands().bitCount(redisKey.getBytes())));
    }

    // 统计3组数据的布尔值，并对这3组数据做OR运算
    @Test
    public void testBitMapOperation() {
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        // OR运算
        String orKey = "test:bm:or";
        System.out.println(redisTemplate.execute((RedisCallback<Object>) con -> {
            con.stringCommands().bitOp(RedisStringCommands.BitOperation.OR, orKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
            return con.stringCommands().bitCount(orKey.getBytes());
        }));
        System.out.println(redisTemplate.opsForValue().getBit(orKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(orKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(orKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(orKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(orKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(orKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(orKey, 6));
    }
}
