package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

// 示例注释掉，因为这个切面是用来测试的，不是用来实际使用的
//@Component
//@Aspect
public class TestAspect {

    // 切入点
    // 第一个*代表返回值，第二个*代表类，第三个*代表方法，..代表参数，com.nowcoder.community.service代表包
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {
    }

    // 前置通知
    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    // 后置通知
    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    // 返回后通知
    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    // 异常后通知
    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    // 环绕通知
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");// 前置
        Object obj = joinPoint.proceed(); // 调用目标组件的方法
        System.out.println("around after");// 后置
        return obj;
    }
}
