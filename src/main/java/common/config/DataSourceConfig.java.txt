package com.wuhan.seckill.common.config;

import com.wuhan.seckill.common.datasource.DynamicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.master.url}")
    private String masterUrl;
    @Value("${spring.datasource.master.username}")
    private String masterUsername;
    @Value("${spring.datasource.master.password}")
    private String masterPassword;
    @Value("${spring.datasource.master.driver-class-name}")
    private String masterDriver;

    @Value("${spring.datasource.slave.url}")
    private String slaveUrl;
    @Value("${spring.datasource.slave.username}")
    private String slaveUsername;
    @Value("${spring.datasource.slave.password}")
    private String slavePassword;
    @Value("${spring.datasource.slave.driver-class-name}")
    private String slaveDriver;

    @Bean
    public DataSource masterDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(masterDriver)
                .url(masterUrl)
                .username(masterUsername)
                .password(masterPassword)
                .build();
    }

    @Bean
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(slaveDriver)
                .url(slaveUrl)
                .username(slaveUsername)
                .password(slavePassword)
                .build();
    }

    @Primary
    @Bean
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        dataSourceMap.put("slave", slaveDataSource());
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        // 默认数据源：主库
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource());
        return dynamicDataSource;
    }
}