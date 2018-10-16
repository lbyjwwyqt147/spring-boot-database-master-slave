package com.example.database.datasource;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/***
 *
 */
@Configuration
@EnableConfigurationProperties
@EnableAutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true) //开启事务管理的注解
public class AtomikosDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.one")
    public DataSource dataSourceOne() {
        return new AtomikosDataSourceBean();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.two")
    public DataSource dataSourceTwo() {
        return new AtomikosDataSourceBean();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.three")
    public DataSource dataSourceThree() {
        return new AtomikosDataSourceBean();
    }

    /**
     * 多个从库
     *
     * @return
     */
    public ConcurrentHashMap<String, DataSource> slaveDataSources() {
        ConcurrentHashMap<String, DataSource> dataSources = new ConcurrentHashMap<>();
        dataSources.put(DataSourceType.SLAVE_0, dataSourceTwo());
        dataSources.put(DataSourceType.SLAVE_1, dataSourceThree());
        return dataSources;
    }


    /**
     * 设置　AbstractRoutingDataSource　包装多个数据源
     *
     * @return
     */
   // @Bean
    @Primary
    public MultipleDataSourceRouting dataSource() throws Throwable{
        //按照目标数据源名称和目标数据源对象的映射存放在Map中
        Map<String, DataSource> targetDataSources = new ConcurrentHashMap<>();
        targetDataSources.put(DataSourceType.MASTER, dataSourceOne());
        slaveDataSources().forEach((k, v) -> {
            targetDataSources.put(k, v);
        });
        //采用是想AbstractRoutingDataSource的对象包装多数据源
        return new MultipleDataSourceRouting(dataSourceOne(), targetDataSources);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws Throwable {
        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
       // entityManager.setDataSource(dataSource());
        entityManager.setDataSource(dataSourceOne());
        entityManager.setJpaVendorAdapter(jpaVendorAdapter());
        entityManager.setPackagesToScan("com.example.database.entity");
       // entityManager.setPersistenceUnitName("jpa");
        entityManager.setPersistenceXmlLocation("classpath:my-persistence.xml");
        entityManager.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        Properties properties = new Properties();
        //jta设置
        properties.put("hibernate.current_session_context_class", "jta");
        properties.put("hibernate.transaction.factory_class", "org.hibernate.transaction.JTATransactionFactory");
        properties.put("hibernate.transaction.manager_lookup_class", "com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");
        entityManager.setJpaProperties(properties);
        return entityManager;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(true);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        return hibernateJpaVendorAdapter;
    }

    @Bean(name = "atomikosUserTransaction")
    public UserTransaction atomikosUserTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(60000);
        return userTransactionImp;
    }

    /**
     *  atomikos事务管理
     * @return
     * @throws Throwable
     */
    @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(true);
        return userTransactionManager;
    }

    /**
     *  JTA 分布式事物　　适用于多个数据源事物管理
     * @return
     * @throws Throwable
     */
    @Bean(name = "transactionManager")
    @Primary
    @DependsOn({ "atomikosUserTransaction", "atomikosTransactionManager" })
    public PlatformTransactionManager transactionManager() throws Throwable {
        UserTransaction userTransaction = atomikosUserTransaction();
        TransactionManager atomikosTransactionManager = atomikosTransactionManager();
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, atomikosTransactionManager);
        jtaTransactionManager.setAllowCustomIsolationLevels(true);
        return jtaTransactionManager;
    }




}
