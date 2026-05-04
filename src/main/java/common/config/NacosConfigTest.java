package com.wuhan.seckill.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@RefreshScope  // 开启Nacos配置动态刷新
public class NacosConfigTest {

    @Value("${seckill.test.config:default-value}")
    private String testConfig;

    @GetMapping("/get")
    public String getConfig() {
        return "当前配置值：" + testConfig;
    }
}