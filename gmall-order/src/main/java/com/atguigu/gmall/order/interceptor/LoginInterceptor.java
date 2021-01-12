package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.common.entity.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sun.security.provider.certpath.OCSPResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {


    public static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        UserInfo userInfo = new UserInfo();
        String userId = request.getHeader("userId");

        userInfo.setUserId(Long.valueOf(userId));
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
