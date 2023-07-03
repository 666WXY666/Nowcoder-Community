package com.nowcoder.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("alphaNewImpl")
@Primary
public class AlphaNewImpl implements AlphaDao {
    @Override
    public String select() {
        return "new";
    }
}
