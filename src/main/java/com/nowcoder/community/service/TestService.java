package com.nowcoder.community.service;

import com.nowcoder.community.dao.TestDao;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    private TestDao td;

    public TestService(@Qualifier("testNewImpl") TestDao td) {
        System.out.println("TestService");
        this.td = td;
    }

    @PostConstruct
    public String init() {
        return "hello test!" + td.select();
    }

    @PreDestroy
    public void destroy() {
        System.out.println("destroy");
    }

    public String find() {
        return td.select();
    }
}
