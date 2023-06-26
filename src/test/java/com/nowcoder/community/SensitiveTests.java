package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        // 普通敏感词
        String text = "这里可以读博,可以嫖娼,可以吸毒...";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        // 特殊符号
        text = "这里可以赌→博→,可以→嫖→娼→,可以吸→毒...fabc";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        // 全部过滤
        text = "fabcd";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        // 全部过滤
        text = "fabcc";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        // 部分过滤
        text = "fabc";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        // 全是符号
        text = "→→→→→";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        // 特殊用例
        text = "☆f☆a☆b☆c☆";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "☆f☆a☆b☆c";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "f☆a☆b☆c";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "f☆ab☆c☆";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "fa☆b☆c☆";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "f☆a☆bc☆";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "f☆a☆bc☆d";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "f☆a☆bc☆d☆";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "☆f☆a☆bc☆d☆";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "qqfabc";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);

        System.out.println(text);
        text = "某个作家有一辆车，他喜欢开车，同时有一本小说叫##赌#博#默示录，\n" +
                "    吸烟有害健康，吸#毒和嫖###娼是很危险的事情，\n" +
                "    不要想法子***, aaabb fabccc abc ...";
        System.out.print(text + "   ->   ");
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
