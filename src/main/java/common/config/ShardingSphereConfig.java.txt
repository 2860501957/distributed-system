package com.wuhan.seckill.common.config;

import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ShardingSphereConfig {

    @Bean
    public DataSource shardingDataSource() throws SQLException {
        // 1. 配置真实数据源（主从库，可根据需要扩展）
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        dataSourceMap.put("slave", slaveDataSource());

        // 2. 配置订单表分库分表规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRule = new TableRuleConfiguration("t_order", "ds${0..1}.t_order${0..1}");

        // 分库策略：按用户ID分库（user_id % 2）
        orderTableRule.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", (PreciseShardingAlgorithm<Long>) (availableTargetNames, shardingValue) -> {
            long userId = shardingValue.getValue();
            return availableTargetNames.stream()
                    .filter(name -> name.endsWith(String.valueOf(userId % 2)))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("无效的数据源"));
        }));

        // 分表策略：按订单ID分表（order_id % 2）
        orderTableRule.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("id", (PreciseShardingAlgorithm<Long>) (availableTargetNames, shardingValue) -> {
            long orderId = shardingValue.getValue();
            return availableTargetNames.stream()
                    .filter(name -> name.endsWith(String.valueOf(orderId % 2)))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("无效的表名"));
        }));

        // 雪花算法ID生成器配置
        KeyGeneratorConfiguration keyGeneratorConfig = new KeyGeneratorConfiguration("SNOWFLAKE", "id");
        orderTableRule.setKeyGeneratorConfig(keyGeneratorConfig);

        shardingRuleConfig.getTableRuleConfigs().add(orderTableRule);

        // 3. 创建 ShardingSphere 数据源
        Properties props = new Properties();
        props.setProperty("sql.show", "true");
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, props);
    }

    // 复用之前的主从数据源配置
    private DataSource masterDataSource() {
        // 直接调用 DataSourceConfig 中的 masterDataSource() 方法即可
        return new DataSourceConfig().masterDataSource();
    }

    private DataSource slaveDataSource() {
        return new DataSourceConfig().slaveDataSource();
    }
}