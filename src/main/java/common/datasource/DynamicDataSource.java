package com.wuhan.seckill.common.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<DataSourceType> DATA_SOURCE_HOLDER = new ThreadLocal<>();

    public static void setDataSourceType(DataSourceType type) {
        DATA_SOURCE_HOLDER.set(type);
    }

    public static DataSourceType getDataSourceType() {
        return DATA_SOURCE_HOLDER.get();
    }

    public static void clearDataSourceType() {
        DATA_SOURCE_HOLDER.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DATA_SOURCE_HOLDER.get();
    }
}