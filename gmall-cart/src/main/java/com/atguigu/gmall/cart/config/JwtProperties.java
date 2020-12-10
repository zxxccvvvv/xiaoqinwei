package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "auth.jwt")
@Data
@Slf4j
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;
    private String userKey;
    private Integer expire;

    private PublicKey publicKey;


    //该方法在构造方法之前执行
    @PostConstruct
    public void init(){
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("生成公钥和私钥出错");
            e.printStackTrace();

        }
    }
}
