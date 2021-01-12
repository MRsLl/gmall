package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {

    /**
     * 根据 userId 查询用户信息
     * @param id
     * @return
     */
    @GetMapping("ums/user/{id}")
    ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);

        /**
         * 根据登录名和密码查询用户
         * @param loginName
         * @param password
         * @return
         */
    @GetMapping("/ums/user/query")
    ResponseVo<UserEntity> queryUser(@RequestParam("loginName") String loginName,@RequestParam("password") String password);

    /**
     * 根据用户id，查询用户收货地址集合
     * @param userId
     * @return
     */
    @GetMapping("ums/useraddress/user/{userId}")
    ResponseVo<List<UserAddressEntity>> queryAddressesByUserId(@PathVariable(value = "userId") Long userId);

}
