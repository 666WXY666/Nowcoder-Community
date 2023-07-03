package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("alphaMyBatisImpl")
public class AlphaMyBatisImpl implements AlphaDao {
    @Override
    public String select() {
        return "MyBatis";
    }
}
