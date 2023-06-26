package com.heima.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(prefix = "minio")
public class MinIOConfigProperties implements Serializable {
    private String accessKey;   // 用户名
    private String secretKey;   // 密码
    private String bucket;      // 桶名称
    private String endpoint;    // minIO地址
    private String readPath;
}
