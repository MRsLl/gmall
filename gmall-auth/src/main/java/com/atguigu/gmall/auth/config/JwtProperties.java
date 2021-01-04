package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private String publicKeyPath;

    private String privateKeyPath;

    private String secret;

    private String cookieName;

    private Integer expire;

    private String unick;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        try {
            File pubFile = new File(publicKeyPath);
            File priFile = new File(privateKeyPath);

            if (!pubFile.exists() || !priFile.exists()) {
                RsaUtils.generateKey(publicKeyPath,privateKeyPath,secret);
            }
            this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        } catch (Exception e) {
            log.error("生成密钥失败");
        }
    }
}
