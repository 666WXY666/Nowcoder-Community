package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("testMyBatisImpl")
public class TestMyBatisImpl implements TestDao {
    @Override
    public String select() {
        return "MyBatis";
    }
}
