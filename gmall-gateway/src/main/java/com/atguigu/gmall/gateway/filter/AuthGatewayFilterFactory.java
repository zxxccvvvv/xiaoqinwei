package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    private JwtProperties jwtProperties;

    /*
    * 告诉父类，这里使用PathConfig对象接受配置内容
    *
    * */
    public AuthGatewayFilterFactory(){
        super(PathConfig.class);
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("我是局部过滤器，我只拦截特定路由的服务请求");
                System.out.println("局部过滤器获取配置信息：key" + config.getAuthPaths());

                //HttpServletRequest --> ServletHttpRequest
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                //1.判断当前请求的路径是否在拦截名单中，不在直接放行
                String curPath = request.getURI().getPath();
                List<String> paths = config.getAuthPaths();
                if (!paths.stream().anyMatch(path -> curPath.startsWith(path))){
                    return chain.filter(exchange);
                }
                //2.获取请求头的token信息，异步-头信息，同步->cookies信息
                String token = request.getHeaders().getFirst("token");
                //如果token为空，尝试从cookies中获取
                if (StringUtils.isBlank(token)){
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())){
                        HttpCookie cookie = request.getCookies().getFirst(jwtProperties.getCookieName());
                        token = cookie.getValue();
                    }
                }

                //3。判断token是否为空，为空则重定向到登录页面
                if (StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete(); //拦截后续业务
                }
                try {
                    //4.解析token信息，如果异常则重定向到登录页面
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    //5.拿到载荷中的ip和请求的ip进行比较，不一致则说明盗用别人的token，则重定向到登录页面
                    String ip = map.get("ip").toString();
                    String curIp = IpUtils.getIpAddressAtGateway(request);
                    if (!StringUtils.equals(ip, curIp)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        response.setComplete();
                    }
                    //6.把解析到的用户登录信息传递给后续服务
                    request.mutate().header("userId", map.get("userId").toString()).build();
                    exchange.mutate().request(request).build();

                    //7.放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /*
    * 使用List集合通过不同的字段分别读取，使用原生的只能读取一个
    * 在这里希望通过一个集合字段读取所有所有的路径
    * */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("authPaths");
    }

    /*
    * 指定读取字段的结果集类型
    * 默认是map的方式。把配置读取到不同的字段
    * */

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }



    /*
    *
    * 读取配置的内部类
    *
    * */
    @Data
    public static class PathConfig{
        private List<String> authPaths;
    }

}
