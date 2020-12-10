package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;


    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        //1.调用远程接口查询用户信息
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();
 
        if (userEntity == null){
            throw new UserException("用户名或者密码输入错误");
        }
        try {
            //3.生成jwt
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", userEntity.getId());
            map.put("username", userEntity.getUsername());
            map.put("ip", IpUtils.getIpAddressAtService(request));
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(),jwtProperties.getExpire());
            //把jwt放入cookie
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token, jwtProperties.getExpire()*60);
            CookieUtils.setCookie(request,response,jwtProperties.getNickName(),userEntity.getNickname(), jwtProperties.getExpire()*60);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
