package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将指定的IP计入UV
    public void recordUV(String ip) {
        redisTemplate.opsForHyperLogLog().add(RedisKeyUtil.getUVKey(df.format(new Date())), ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        } else if (start.after(end)) {
            return -1;
        }

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            keyList.add(RedisKeyUtil.getUVKey(df.format(calendar.getTime())));
            calendar.add(Calendar.DATE, 1);
        }

        // 合并这些数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(int userId) {
        redisTemplate.opsForValue().setBit(RedisKeyUtil.getDAUKey(df.format(new Date())), userId, true);
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        } else if (start.after(end)) {
            return -1;
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            keyList.add(RedisKeyUtil.getDAUKey(df.format(calendar.getTime())).getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 合并这些数据，返回统计的结果
        String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
        Object obj = redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.stringCommands().bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), keyList.toArray(new byte[0][0]));
            return connection.stringCommands().bitCount(redisKey.getBytes());
        });
        return obj == null ? 0 : (long) obj;
    }
}
