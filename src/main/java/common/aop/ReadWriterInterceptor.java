package com.wuhan.seckill.common.aop;

import com.wuhan.seckill.common.datasource.DataSourceType;
import com.wuhan.seckill.common.datasource.DynamicDataSource;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ReadWriteInterceptor {

    // 读操作：切到从库
    @Before("execution(* com.wuhan.seckill.*.service.*.get*(..)) || " +
            "execution(* com.wuhan.seckill.*.service.*.list*(..)) || " +
            "execution(* com.wuhan.seckill.*.service.*.query*(..)) || " +
            "execution(* com.wuhan.seckill.*.service.*.select*(..))")
    public void setSlave() {
        DynamicDataSource.setDataSourceType(DataSourceType.SLAVE);
    }

    // 写操作：切到主库
    @Before("execution(* com.wuhan.seckill.*.service.*.insert*(..)) || " +
            "execution(* com.wuhan.seckill.*.service.*.add*(..)) || " +
            "execution(* com.wuhan.seckill.*.service.*.update*(..)) || " +
            "execution(* com.wuhan.seckill.*.service.*.delete*(..)) || " +
            "execution(* com.wuhan.seckill.*.service.*.save*(..))")
    public void setMaster() {
        DynamicDataSource.setDataSourceType(DataSourceType.MASTER);
    }

    // 方法执行后：清除数据源，避免线程复用污染
    @After("execution(* com.wuhan.seckill.*.service.*.*(..))")
    public void clear() {
        DynamicDataSource.clearDataSourceType();
    }
}