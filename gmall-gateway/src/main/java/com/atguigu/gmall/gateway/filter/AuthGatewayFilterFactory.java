package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import com.google.common.net.HttpHeaders;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 一定要重写构造方法
     * 告诉父类，这里使用PathConfig对象接收配置内容
     */
    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        // 实现GatewaFilter接口
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                //1.判断请求路径在不在拦截名单内，不在直接放行
                String path = request.getURI().getPath();
                if (config.authPaths.stream().allMatch(authPath -> path.indexOf(authPath) == -1 )) {
                    return chain.filter(exchange);
                }

                //2.获取请求中的token，同步请求从cookie 中获取，异步请求从header 中获取
                String token = request.getHeaders().getFirst("token");

                if (StringUtils.isBlank(token)) {
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();

                    if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())) {
                        token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                    }
                }

                //3.判断token 是否为空，为空直接拦截
                if (StringUtils.isEmpty(token)) {
                    //重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }

                try {
                    //4.若不为空，解析jwt 获取登录信息，解析异常直接拦截
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

                    //5.判断是否被盗用
                    String ip = (String)map.get("ip");
                    String curIp = IpUtil.getIpAddressAtGateway(request);

                    if (!StringUtils.equals(ip,curIp)) {
                        //重定向到登录页面
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        return response.setComplete();
                    }

                    //6.传递登录信息给后续服务
                    //把userId 转化为request 对象中的头信息
                    request.mutate().header("userId", map.get("userId").toString()).build();
                    //把新的request 对象转化为exchange 对象
                    exchange.mutate().request(request).build();

                    //7.放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    //重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }
            }
        };
    }

    /**
     * 指定字段顺序
     * 可以通过不同的字段分别读取：/toLogin.html,/login
     * 在这里希望通过一个集合字段读取所有的路径
     * @return
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("authPaths");
    }

    /**
     * 指定读取字段的结果集类型
     * 默认通过map的方式，把配置读取到不同字段
     *  例如：/toLogin.html,/login
     *      由于只指定了一个字段，只能接收/toLogin.html
     * @return
     */
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    /**
     * 读取配置的内部类
     */
    @Data
    public static class PathConfig{
        private List<String> authPaths;
    }
}
