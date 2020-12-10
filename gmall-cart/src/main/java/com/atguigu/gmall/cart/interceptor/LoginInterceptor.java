package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("前置方法。。。。。。。。。。。");
        UserInfo userInfo = new UserInfo();
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());
        if (StringUtils.isBlank(userKey)){
            userKey  = UUID.randomUUID().toString();
            CookieUtils.setCookie(request,response,jwtProperties.getUserKey(),userKey, jwtProperties.getExpire());
        }
        userInfo.setUserKey(userKey);

        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        if (StringUtils.isNotBlank(token)){
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            userInfo.setUserId(Long.valueOf(map.get("userId").toString()));
        }

        THREAD_LOCAL.set(userInfo);
        return true;

    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //这里适用了Tomcat的线程池，如果不显示的删除THREAD_LOCAL会造成内存泄漏，长时间会导致OOM
        THREAD_LOCAL.remove();
    }
}
