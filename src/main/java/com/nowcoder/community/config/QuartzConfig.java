package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;
import com.nowcoder.community.quartz.WkImageDeleteJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程:
    // 1. 通过FactoryBean封装Bean的实例化过程
    // 2. 将FactoryBean装配到Spring容器里
    // 3. 将FactoryBean注入给其他的Bean
    // 4. 该Bean得到的是FactoryBean所管理的对象实例

    // 配置JobDetail
    // @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 设置Job的类型
        factoryBean.setJobClass(AlphaJob.class);
        // 设置Job的名称
        factoryBean.setName("alphaJob");
        // 设置Job的组名
        factoryBean.setGroup("alphaJobGroup");
        // 设置Job的持久性
        factoryBean.setDurability(true);
        // 设置Job的是否可恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
    // @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 设置触发器关联的JobDetail
        factoryBean.setJobDetail(alphaJobDetail);
        // 设置触发器的名称
        factoryBean.setName("alphaTrigger");
        // 设置触发器的组名
        factoryBean.setGroup("alphaTriggerGroup");
        // 设置触发器的执行周期
        factoryBean.setRepeatInterval(3000);
        // 设置触发器的是否可恢复
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    // 配置JobDetail，刷新帖子分数
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 设置Job的类型
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        // 设置Job的名称
        factoryBean.setName("postScoreRefreshJob");
        // 设置Job的组名
        factoryBean.setGroup("communityJobGroup");
        // 设置Job的持久性
        factoryBean.setDurability(true);
        // 设置Job的是否可恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 配置Trigger，刷新帖子分数
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 设置触发器关联的JobDetail
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        // 设置触发器的名称
        factoryBean.setName("postScoreRefreshTrigger");
        // 设置触发器的组名
        factoryBean.setGroup("communityTriggerGroup");
        // 设置触发器的执行周期（5min）
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        // 设置触发器的是否可恢复
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    // 删除WK图片任务
    @Bean
    public JobDetailFactoryBean wkImageDeleteJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(WkImageDeleteJob.class);
        factoryBean.setName("wkImageDeleteJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 删除WK图片触发器
    @Bean
    public SimpleTriggerFactoryBean wkImageDeleteTrigger(JobDetail wkImageDeleteJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(wkImageDeleteJobDetail);
        factoryBean.setName("wkImageDeleteTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 4);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
