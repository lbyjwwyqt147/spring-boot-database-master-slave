package com.example.database.datasource;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/***
 * 全局 事物配置  利用AOP
 */
@Aspect
@Configuration
public class TransactionAdviceConfig {

    private static final String AOP_POINTCUT_EXPRESSION = "execution (* com.example.database.service.*.*(..))";

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     *
     * @return
     */
    @Bean
    public TransactionInterceptor txAdvice() {
        // 写操作的 事物传播
        DefaultTransactionAttribute writePropagation = new DefaultTransactionAttribute();
        writePropagation.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 读操作的 事物传播
        DefaultTransactionAttribute readPropagation = new DefaultTransactionAttribute();
        readPropagation.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        readPropagation.setReadOnly(true);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        source.addTransactionalMethod("add*", writePropagation);
        source.addTransactionalMethod("save*", writePropagation);
        source.addTransactionalMethod("delete*", writePropagation);
        source.addTransactionalMethod("update*", writePropagation);
        source.addTransactionalMethod("insert*", writePropagation);
        source.addTransactionalMethod("exec*", writePropagation);
        source.addTransactionalMethod("set*", writePropagation);
        source.addTransactionalMethod("exists*", readPropagation);
        source.addTransactionalMethod("get*", readPropagation);
        source.addTransactionalMethod("query*", readPropagation);
        source.addTransactionalMethod("find*", readPropagation);
        source.addTransactionalMethod("list*", readPropagation);
        source.addTransactionalMethod("count*", readPropagation);
        source.addTransactionalMethod("is*", readPropagation);
        source.addTransactionalMethod("read*", readPropagation);
        return new TransactionInterceptor(transactionManager, source);
    }

    /**
     *
     * @return
     */
    @Bean
    public Advisor txAdviceAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdvice());
    }

}
