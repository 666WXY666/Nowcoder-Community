package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("testNewImpl")
public class TestNewImpl implements TestDao {
    @Override
    public String select() {
        return "new";
    }
}
