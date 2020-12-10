package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        //初始化Cors配置对象
        CorsConfiguration config = new CorsConfiguration();
        //允许的域, 不要写*, 负责cookie就无法使用
        config.addAllowedOrigin("http://manager.gmall.com");
        config.addAllowedOrigin("http://www.gmall.com");
        config.addAllowedOrigin("http://gmall.com");
        //允许头信息
        config.addAllowedHeader("*");
        //允许请求方式
        config.addAllowedMethod("*");
        //是否请求携带cookie
        config.setAllowCredentials(true);

        //添加映射路径,我们拦截所有请求
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(corsConfigurationSource);
    }
}
