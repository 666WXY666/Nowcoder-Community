package com.nowcoder.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("testNewImpl")
@Primary
public class TestNewImpl implements TestDao {
    @Override
    public String select() {
        return "new";
    }
}
