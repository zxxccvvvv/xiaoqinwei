package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.cart.pojo.UserInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();

        String userId = request.getHeader("userId");
        userInfo.setUserId(Long.valueOf(userId));

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
