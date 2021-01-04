package com.atguigu.gmall.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {
    void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response);
}
