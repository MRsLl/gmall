package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface GmallUmsApi {

    /**
     * 根据登录名和密码查询用户
     * @param loginName
     * @param password
     * @return
     */
    @GetMapping("/ums/user/query")
    ResponseVo<UserEntity> queryUser(@RequestParam("loginName") String loginName,@RequestParam("password") String password);
}
