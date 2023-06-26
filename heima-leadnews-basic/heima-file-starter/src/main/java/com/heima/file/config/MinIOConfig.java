package com.heima.file.config;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Data
@Configuration
@EnableConfigurationProperties({MinIOConfigProperties.class})
public class MinIOConfig {
    @Resource
    private MinIOConfigProperties minIOConfigProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .credentials(minIOConfigProperties.getAccessKey(), minIOConfigProperties.getSecretKey())
                .endpoint(minIOConfigProperties.getEndpoint())
                .build();
    }
}
