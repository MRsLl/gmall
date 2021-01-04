package com.atguigu.gmall.ums.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云sms 短信验证码配置类
 */
@Data
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliYunSmsProperties {

    private String signName;
    private String templateCode;
    private String codePrefix;
    private String keyId;
    private String keySecret;
}
