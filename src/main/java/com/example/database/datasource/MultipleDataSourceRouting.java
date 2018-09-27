package com.example.database.datasource;

import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/***
 *  多数据源 切换  根据标识获取不同源
 *  其他说明:  通过扩展AbstractRoutingDataSource来获取不同的源。它是Spring提供的一个可以根据用户发起的不同请求去转换不同的数据源，比如根据用户的不同地区语言选择不同的数据库。通过查看源码可以发现，它是通过determineCurrentLookupKey（）返回的不同key到sqlSessionFactory中获取不同源
 */
@Slf4j
public class MultipleDataSourceRouting extends AbstractRoutingDataSource {


    public MultipleDataSourceRouting(DataSource defaultTargetDataSource, Map<String, DataSource> targetDataSources) {
        //设置默认的数据源，当拿不到数据源时，使用此配置
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(new HashMap<>(targetDataSources));
        // 必须 将　targetDataSources　的　DataSource　加载到　resolvedDataSources
        super.afterPropertiesSet();
    }

    /**
     * 根据Key获取数据源名称
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String lookupKey = DataSourceContextHolder.getDataSource();
        log.info("执行多数据源 切换  当前数据源:" + (StringUtils.isEmpty(lookupKey) ? "null 使用默认数据源" : lookupKey));
        return lookupKey;
    }


}
