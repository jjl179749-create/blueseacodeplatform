package org.blueseacode.service.file.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    @Bean
    public OSS ossClient() {
        // endpoint 配置为域名格式（无协议头），此处补上 https://
        // 阿里云 OSS 已强制要求 HTTPS，使用 HTTP 会连接被拒
        String endpointWithProtocol = endpoint.startsWith("http") ? endpoint : "https://" + endpoint;
        return new OSSClientBuilder().build(endpointWithProtocol, accessKeyId, accessKeySecret);
    }
}
