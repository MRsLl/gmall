package com.atguigu.gmall.ums.controller;

import java.util.List;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 用户表
 *
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 20:01:29
 */
@Api(tags = "用户表 管理")
@RestController
@RequestMapping("ums/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("query")
    @ApiOperation("根据登录名和密码查询用户")
    public ResponseVo<UserEntity> queryUser(@RequestParam("loginName") String loginName,@RequestParam("password") String password) {
        UserEntity userEntity = userService.queryUser(loginName,password);

        return ResponseVo.ok(userEntity);
    }

    @PostMapping("register")
    @ApiOperation("用户注册")
    public ResponseVo register( UserEntity userEntity,@RequestParam("code") String code) {
        userService.register(userEntity,code);

        return ResponseVo.ok();
    }

    @PostMapping("code")
    @ApiOperation("生成并发送短信验证码")
    public ResponseVo sendCode(@RequestParam String phone) {

        userService.sendCode(phone);
        return ResponseVo.ok();
    }

    @GetMapping("check/{data}/{type}")
    @ApiOperation("用户数据校验")
    public ResponseVo<Boolean> checkData(@PathVariable(value = "data") String data,
                                         @PathVariable(value = "type") Integer type) {
        Boolean flag = userService.checkData(data,type);
        return ResponseVo.ok(flag);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = userService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id){
		UserEntity user = userService.getById(id);

        return ResponseVo.ok(user);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserEntity user){
		userService.save(user);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserEntity user){
		userService.updateById(user);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		userService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
