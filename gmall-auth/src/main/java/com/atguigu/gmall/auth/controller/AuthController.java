package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl",required = false) String returnUrl, Model model) {
        //把登录前的页面记录下来，登录完成后回跳
        model.addAttribute("returnUrl",returnUrl);

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password,
            @RequestParam(value = "returnUrl",required = false,defaultValue = "http://gmall.com") String returnUrl,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.login(loginName,password,request,response);
        return "redirect:" + returnUrl;
    }
}
