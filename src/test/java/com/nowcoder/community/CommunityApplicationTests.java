package com.nowcoder.community;

import com.nowcoder.community.dao.TestDao;
import com.nowcoder.community.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private SimpleDateFormat sdf;

    @Autowired
    @Qualifier("testNewImpl")
    private TestDao testDao;

    @Autowired
    private TestService testService;

    @Test
    void contextLoads() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);

        TestDao testDao = applicationContext.getBean(TestDao.class);
        System.out.println(testDao.select());

        testDao = applicationContext.getBean("testNewImpl", TestDao.class);
        System.out.println(testDao.select());
    }

    @Test
    public void testDI() {
        System.out.println(testDao);
        System.out.println(testService);
        System.out.println(sdf);
    }
}
