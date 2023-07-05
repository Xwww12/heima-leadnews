package com.heima.wemedia.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描feign服务降级的包
 */
@Configuration
@ComponentScan("com.heima.apis.article.fallback")
public class InitConfig {
}