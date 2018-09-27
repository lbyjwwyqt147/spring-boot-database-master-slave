package com.example.database.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
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

        /**
         * 　声明主库 Bean实例
         *
         * @return
         */
        @Bean(value = "masterDataSource", destroyMethod = "close")
        public DataSource masterDataSource() {
            return getDataSource(masterUrl, masterUsername, masterPassword);
        }

        /**
         * 　声明从库1 Bean实例
         *
         * @return
         */
        @Bean(value = "slaveOneDataSource", destroyMethod = "close")
        public DataSource slaveOneDataSource() {
            return getDataSource(slaveOneUrl, slaveOneUsername, slaveOnePassword);
        }

        /**
         * 　声明从库2 Bean实例
         *
         * @return
         */
        @Bean(value = "slaveTwoDataSource", destroyMethod = "close")
        public DataSource slaveTwoDataSource() {
            return getDataSource(slaveTwoUrl, slaveTwoUsername, slaveTwoPassword);
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
        public MultipleDataSourceRouting dataSource() throws SQLException{
            //按照目标数据源名称和目标数据源对象的映射存放在Map中
            Map<String, DataSource> targetDataSources = new ConcurrentHashMap<>();
            targetDataSources.put(DataSourceType.MASTER, masterDataSource());
            slaveDataSources().forEach((k, v) -> {
                targetDataSources.put(k, v);
            });
            //采用是想AbstractRoutingDataSource的对象包装多数据源
            return new MultipleDataSourceRouting(masterDataSource(), targetDataSources);
        }


        /**
         * 　DataSource　事物
         *
         * @param dataSource
         * @return
         */
        @Bean
        @Primary
        public PlatformTransactionManager transactionManager(DataSource dataSource) throws SQLException{
            dataSource = dataSource();
            return new DataSourceTransactionManager(dataSource);
        }

        /**
         * jpa 事物
         *
         * @param entityManagerFactory
         * @return
         */
        @Bean
        public PlatformTransactionManager txManager(EntityManagerFactory entityManagerFactory) throws SQLException{
            return new JpaTransactionManager(entityManagerFactory);
        }


        /**
         *   配置DruidDataSource
         *
         * @param url
         * @param username
         * @param password
         * @return
         */
        public DataSource getDataSource(String url, String username, String password) {
            DruidDataSource datasource = new DruidDataSource();
            datasource.setUrl(url);
            datasource.setUsername(username);
            datasource.setPassword(password);
            datasource.setDriverClassName(driverClassName);

            //configuration
            datasource.setInitialSize(initialSize);
            datasource.setMinIdle(minIdle);
            datasource.setMaxActive(maxActive);
            datasource.setMaxWait(maxWait);
            datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            datasource.setValidationQuery(validationQuery);
            datasource.setTestWhileIdle(testWhileIdle);
            datasource.setTestOnBorrow(testOnBorrow);
            datasource.setTestOnReturn(testOnReturn);
            datasource.setPoolPreparedStatements(poolPreparedStatements);
            datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
            datasource.setUseGlobalDataSourceStat(useGlobalDataSourceStat);
            try {
                datasource.setFilters(filters);
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("druid configuration initialization filter: " + e);
            }
            datasource.setConnectionProperties(connectionProperties);
            return datasource;
        }

    }

}
