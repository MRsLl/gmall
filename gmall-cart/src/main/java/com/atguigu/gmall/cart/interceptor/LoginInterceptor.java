package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.common.entity.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;

    public static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        UserInfo userInfo = new UserInfo();

        //1.从cookie 中获取userKey
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());

        if (StringUtils.isBlank(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request,response,jwtProperties.getUserKey(),userKey,jwtProperties.getExpire());
        }
        userInfo.setUserKey(userKey);

        //2.从cookie 中获取token 信息
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        if (!StringUtils.isBlank(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            Long userId =  Long.parseLong(map.get("userId").toString());
            userInfo.setUserId(userId);
        }

        //3.将userKey 和 userId 设置到 ThreadLocal 静态变量中
        THREAD_LOCAL.set(userInfo);

        return true;
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 @Nullable Exception ex) throws Exception {

        THREAD_LOCAL.remove();
    }
}
