package com.wuhan.seckill.common.datasource;

public enum DataSourceType {
    MASTER,  // 主库：写操作
    SLAVE    // 从库：读操作
}