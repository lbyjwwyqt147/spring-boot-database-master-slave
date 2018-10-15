package com.example.database.datasource;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
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
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/***
 *  配置Druid
 */
@Slf4j
@Configuration
@EnableTransactionManagement(proxyTargetClass = true) //开启事务管理的注解
@Data
public class DruidConfiguration {

    @Value("${spring.datasource.stat-view-servlet.allow}")
    private String servletAllow;
    @Value("${spring.datasource.stat-view-servlet.deny}")
    private String servletDeny;
    @Value("${spring.datasource.stat-view-servlet.enabled}")
    private Boolean servletEnabled;
    @Value("${spring.datasource.stat-view-servlet.login-password}")
    private String servletPassword;
    @Value("${spring.datasource.stat-view-servlet.login-username}")
    private String servletUsername;
    @Value("${spring.datasource.stat-view-servlet.reset-enable}")
    private String servletResetEnable;
    @Value("${spring.datasource.web-stat-filter.exclusions}")
    private String webStatExclusions;
    @Value("${spring.datasource.web-stat-filter.enabled}")
    private Boolean webStatEnabled;


    /**
     *  注册DruidServlet
     * @return
     */
    @Bean
    public ServletRegistrationBean druidServlet() {
        log.info("init Druid Servlet Configuration ");
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        // IP白名单
        servletRegistrationBean.addInitParameter("allow", servletAllow);
        // IP黑名单(共同存在时，deny优先于allow)
        servletRegistrationBean.addInitParameter("deny", servletDeny);
        //控制台管理用户
        servletRegistrationBean.addInitParameter("loginUsername", servletUsername);
        servletRegistrationBean.addInitParameter("loginPassword", servletPassword);
        //是否能够重置数据 禁用HTML页面上的“Reset All”功能
        servletRegistrationBean.addInitParameter("resetEnable", servletResetEnable);
        return servletRegistrationBean;
    }

    /**
     * 注册DruidFilter拦截
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        //设置忽略请求
        filterRegistrationBean.addInitParameter("exclusions", webStatExclusions);
        return filterRegistrationBean;
    }


    @Data
    @ConfigurationProperties(prefix = "spring.datasource")
    class DruidDataSourceProperties {

        // 主库 url　
        @Value("${spring.datasource.master.url}")
        private String masterUrl;
        //　主库　用户
        @Value("${spring.datasource.master.username}")
        private String masterUsername;
        //　主库　密码
        @Value("${spring.datasource.master.password}")
        private String masterPassword;

        // 从库１ url　
        @Value("${spring.datasource.slave1.url}")
        private String slaveOneUrl;
        //　从库１　用户
        @Value("${spring.datasource.slave1.username}")
        private String slaveOneUsername;
        //　从库１　密码
        @Value("${spring.datasource.slave1.password}")
        private String slaveOnePassword;

        // 从库2 url　
        @Value("${spring.datasource.slave2.url}")
        private String slaveTwoUrl;
        //　从库2　用户
        @Value("${spring.datasource.slave2.username}")
        private String slaveTwoUsername;
        //　从库2　密码
        @Value("${spring.datasource.slave2.password}")
        private String slaveTwoPassword;

        private String driverClassName;
        private String type;
        private Integer initialSize;
        private Integer minIdle;
        private Integer maxActive;
        private Integer maxWait;
        private Integer timeBetweenEvictionRunsMillis;
        private Integer minEvictableIdleTimeMillis;
        private String validationQuery;
        private Boolean testWhileIdle;
        private Boolean testOnBorrow;
        private Boolean testOnReturn;
        private Boolean poolPreparedStatements;
        private Integer maxPoolPreparedStatementPerConnectionSize;
        private String filters;
        private String connectionProperties;
        private Boolean useGlobalDataSourceStat;
        private Integer poolSize;
        private Integer transactionTimeout;


        /**
         * 　声明主库 Bean实例
         *
         * @return
         */
        @Bean(value = "masterDataSource", initMethod = "init",  destroyMethod = "close")
        public DataSource masterDataSource() {
            return getDataSource(masterUrl, masterUsername, masterPassword, "masterDataSource");
        }

        /**
         * 　声明从库1 Bean实例
         *
         * @return
         */
        @Bean(value = "slaveOneDataSource",  initMethod = "init",  destroyMethod = "close")
        public DataSource slaveOneDataSource() {
            return getDataSource(slaveOneUrl, slaveOneUsername, slaveOnePassword, "slaveOneDataSource");
        }

        /**
         * 　声明从库2 Bean实例
         *
         * @return
         */
        @Bean(value = "slaveTwoDataSource",  initMethod = "init",  destroyMethod = "close")
        public DataSource slaveTwoDataSource() {
            return getDataSource(slaveTwoUrl, slaveTwoUsername, slaveTwoPassword, "slaveTwoDataSource");
        }

        /**
         * 多个从库
         *
         * @return
         */
        @Bean(value = "slaveDataSource")
        public ConcurrentHashMap<String, DataSource> slaveDataSources() {
            ConcurrentHashMap<String, DataSource> dataSources = new ConcurrentHashMap<>();
            dataSources.put(DataSourceType.SLAVE_0, slaveOneDataSource());
            dataSources.put(DataSourceType.SLAVE_1, slaveTwoDataSource());
            return dataSources;
        }

        /**
         * 设置　AbstractRoutingDataSource　包装多个数据源
         *
         * @return
         */
        @Bean(name = "dataSource")
        @Primary
        public MultipleDataSourceRouting dataSource() throws Throwable{
            //按照目标数据源名称和目标数据源对象的映射存放在Map中
            Map<String, DataSource> targetDataSources = new ConcurrentHashMap<>();
            targetDataSources.put(DataSourceType.MASTER, masterDataSource());
            slaveDataSources().forEach((k, v) -> {
                targetDataSources.put(k, v);
            });
            //采用是想AbstractRoutingDataSource的对象包装多数据源
            return new MultipleDataSourceRouting(masterDataSource(), targetDataSources);
        }


      //  @DependsOn({ "atomikosUserTransaction", "atomikosTransactionManager" })
        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws Throwable {
            LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
            //entityManager.setDataSource(dataSource());
            entityManager.setDataSource(masterDataSource());
            entityManager.setJpaVendorAdapter(jpaVendorAdapter());
            entityManager.setPackagesToScan("com.example.database.entity");
            entityManager.setPersistenceUnitName("jpa");
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
            userTransactionImp.setTransactionTimeout(transactionTimeout);
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
            userTransactionManager.setForceShutdown(false);
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




        /**
         * 　DataSourceTransactionManager　事物  只会管理一个数据源的事物　当一个方法中实现了访问多个数据库无法进行多数据源的事物支持。
         *
         * @param dataSource
         * @return
         */
       /* @Bean
        @Primary
        public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) throws Throwable{
            return new DataSourceTransactionManager(dataSource);
        }*/

        /**
         * jpa 事物
         *
         * @param entityManagerFactory
         * @return
         */
        /*@Bean
        public PlatformTransactionManager txManager(EntityManagerFactory entityManagerFactory) throws Throwable{
            return new JpaTransactionManager(entityManagerFactory);
        }*/


        /**
         *   配置DruidDataSource
         *
         * @param url
         * @param username
         * @param password
         * @return
         */
        private DataSource getDataSource(String url, String username, String password) {
           // DruidDataSource dataSource = new DruidDataSource();
            DruidXADataSource dataSource = new DruidXADataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName(driverClassName);

            //configuration
            dataSource.setInitialSize(initialSize);
            dataSource.setMinIdle(minIdle);
            dataSource.setMaxActive(maxActive);
            dataSource.setMaxWait(maxWait);
            dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            dataSource.setValidationQuery(validationQuery);
            dataSource.setTestWhileIdle(testWhileIdle);
            dataSource.setTestOnBorrow(testOnBorrow);
            dataSource.setTestOnReturn(testOnReturn);
            dataSource.setPoolPreparedStatements(poolPreparedStatements);
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
            dataSource.setUseGlobalDataSourceStat(useGlobalDataSourceStat);
            try {
                dataSource.setFilters(filters);
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("druid configuration initialization filter: " + e);
            }
            dataSource.setConnectionProperties(connectionProperties);

            return dataSource;
        }


        /**
         *   配置DruidDataSource
         *
         * @param url
         * @param username
         * @param password
         * @return
         */
        private DataSource getDataSource(String url, String username, String password, String resourceName) {
            // DruidDataSource dataSource = new DruidDataSource();



           /* DruidXADataSource dataSource = new DruidXADataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName(driverClassName);

            //configuration
            dataSource.setInitialSize(initialSize);
            dataSource.setMinIdle(minIdle);
            dataSource.setMaxActive(maxActive);
            dataSource.setMaxWait(maxWait);
            dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            dataSource.setValidationQuery(validationQuery);
            dataSource.setTestWhileIdle(testWhileIdle);
            dataSource.setTestOnBorrow(testOnBorrow);
            dataSource.setTestOnReturn(testOnReturn);
            dataSource.setPoolPreparedStatements(poolPreparedStatements);
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
            dataSource.setUseGlobalDataSourceStat(useGlobalDataSourceStat);
            try {
                dataSource.setFilters(filters);
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("druid configuration initialization filter: " + e);
            }
            dataSource.setConnectionProperties(connectionProperties);*/


           // AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
            AtomikosNonXADataSourceBean xaDataSource = new AtomikosNonXADataSourceBean();
            xaDataSource.setDriverClassName(driverClassName);
            xaDataSource.setPassword(password);
            xaDataSource.setUrl(url);
            xaDataSource.setUser(username);
         //   xaDataSource.setXaDataSource(dataSource);
            //  UniqueResourceName 任意命名，但必须唯一
            xaDataSource.setUniqueResourceName(resourceName);
            xaDataSource.setTestQuery(validationQuery);
         //   xaDataSource.setXaDataSourceClassName(type);
            xaDataSource.setPoolSize(poolSize);
            try {
                xaDataSource.setLoginTimeout(60);  //设置登录超时时间最长为60s
                xaDataSource.setBorrowConnectionTimeout(60);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return xaDataSource;
        }


        /**
         *  AtomikosDataSource
         * @param env
         * @param resourceName
         * @return
         */
        private DataSource atomikosDataSource(Environment env, String resourceName) {
            AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
            Properties prop = build(env, "spring.datasource.");
            dataSource.setXaDataSourceClassName("com.alibaba.druid.pool.xa.DruidXADataSource");
            dataSource.setUniqueResourceName(resourceName);
            dataSource.setPoolSize(5);
            dataSource.setXaProperties(prop);
            return dataSource;

        }

        /**
         *
         * @param env
         * @param prefix
         * @return
         */
        private Properties build(Environment env, String prefix) {
            Properties prop = new Properties();
            prop.put("url", env.getProperty(prefix + "url"));
            prop.put("username", env.getProperty(prefix + "username"));
            prop.put("password", env.getProperty(prefix + "password"));
            prop.put("driverClassName", env.getProperty(prefix + "driverClassName", ""));
            prop.put("initialSize", env.getProperty(prefix + "initialSize", Integer.class));
            prop.put("maxActive", env.getProperty(prefix + "maxActive", Integer.class));
            prop.put("minIdle", env.getProperty(prefix + "minIdle", Integer.class));
            prop.put("maxWait", env.getProperty(prefix + "maxWait", Integer.class));
            prop.put("poolPreparedStatements", env.getProperty(prefix + "poolPreparedStatements", Boolean.class));

            prop.put("maxPoolPreparedStatementPerConnectionSize",
                    env.getProperty(prefix + "maxPoolPreparedStatementPerConnectionSize", Integer.class));

            prop.put("maxPoolPreparedStatementPerConnectionSize",
                    env.getProperty(prefix + "maxPoolPreparedStatementPerConnectionSize", Integer.class));
            prop.put("validationQuery", env.getProperty(prefix + "validationQuery"));
            prop.put("validationQueryTimeout", env.getProperty(prefix + "validationQueryTimeout", Integer.class));
            prop.put("testOnBorrow", env.getProperty(prefix + "testOnBorrow", Boolean.class));
            prop.put("testOnReturn", env.getProperty(prefix + "testOnReturn", Boolean.class));
            prop.put("testWhileIdle", env.getProperty(prefix + "testWhileIdle", Boolean.class));
            prop.put("timeBetweenEvictionRunsMillis",
                    env.getProperty(prefix + "timeBetweenEvictionRunsMillis", Integer.class));
            prop.put("minEvictableIdleTimeMillis", env.getProperty(prefix + "minEvictableIdleTimeMillis", Integer.class));
            prop.put("filters", env.getProperty(prefix + "filters"));

            return prop;
        }

    }

}
