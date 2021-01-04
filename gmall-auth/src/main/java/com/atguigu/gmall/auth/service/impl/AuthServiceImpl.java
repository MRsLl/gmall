package com.atguigu.gmall.auth.service.impl;

import com.alibaba.nacos.client.utils.IPUtil;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Service
@EnableConfigurationProperties({JwtProperties.class})
public class AuthServiceImpl implements AuthService {

    @Resource
    private JwtProperties jwtProperties;
    @Autowired
    private GmallUmsClient umsClient;

    @Override
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {

        try {
            //1.调用用户中心接口，验证用户登录名和密码
            UserEntity userEntity = umsClient.queryUser(loginName, password).getData();
            if (userEntity == null) {
                return;
            }

            //2.校验成功，生成jwt
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",userEntity.getId());
            map.put("username",userEntity.getUsername());

            //为防止 jwt 被盗用，把用户ip 放入jwt 载荷中
            String ip = IpUtil.getIpAddressAtService(request);
            map.put("ip",ip);

            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            //3.jwt 和用户昵称 unick 放入cookie 中
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire()*60);
            CookieUtils.setCookie(request,response,jwtProperties.getUnick(),userEntity.getNickname(),jwtProperties.getExpire()*60);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
