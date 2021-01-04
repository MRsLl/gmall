package com.atguigu.gmall.ums.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gmall.ums.exception.UserException;
import com.atguigu.gmall.ums.properties.AliYunSmsProperties;
import com.atguigu.gmall.ums.util.RandomUtils;
import com.netflix.client.ClientException;
import io.netty.handler.codec.http2.Http2CodecUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service("userService")
@EnableConfigurationProperties({AliYunSmsProperties.class})
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource
    private AliYunSmsProperties aliYunSmsProperties;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();

        switch (type) {
            case 1: queryWrapper.eq("username",data); break;
            case 2: queryWrapper.eq("phone",data); break;
            case 3: queryWrapper.eq("email",data); break;
            default: break;
        }

        return baseMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public void sendCode(String phone) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", aliYunSmsProperties.getKeyId(), aliYunSmsProperties.getKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", aliYunSmsProperties.getSignName());
        request.putQueryParameter("TemplateCode", aliYunSmsProperties.getTemplateCode());

        //生成四位随机验证码，放入缓存
        String code = RandomUtils.getSixBitRandom();
        redisTemplate.opsForValue().set(aliYunSmsProperties.getCodePrefix() + ":" + phone,code,5, TimeUnit.MINUTES);
        request.putQueryParameter("TemplateParam", "{\"code\":"+ code +"}");

        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送短信失败，异常信息为：" + e.getMessage());
        }
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        //1.校验短信验证码
        String phone = userEntity.getPhone();
        if (StringUtils.isBlank(phone)) {
            return;
        }
        String cacheCode = redisTemplate.opsForValue().get(aliYunSmsProperties.getCodePrefix() + ":" + phone);
        if (!StringUtils.equals(code,cacheCode)) {
            return;
        }

        //2.生成盐
        String salt = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
        userEntity.setSalt(salt);

        //3.对用户输入的密码加盐加密
        userEntity.setPassword(DigestUtils.md5Hex(salt + DigestUtils.md5Hex(userEntity.getPassword())));

        //4.保存用户数据
        userEntity.setCreateTime(new Date());
        userEntity.setStatus(1);
        userEntity.setGrowth(100);
        userEntity.setIntegration(100);
        userEntity.setSourceType(1);
        userEntity.setLevelId(1l);
        userEntity.setNickname(userEntity.getUsername());

        baseMapper.insert(userEntity);
        redisTemplate.delete(aliYunSmsProperties.getCodePrefix() + ":" + phone);
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",loginName).or().eq("phone",loginName).or().eq("email",loginName);

        UserEntity userEntity = baseMapper.selectOne(queryWrapper);
        if (userEntity == null) {
            throw new UserException("账户输入不合法！");
        }
        String userPassword = userEntity.getPassword();
        String salt = userEntity.getSalt();

        if (!DigestUtils.md5Hex(salt + DigestUtils.md5Hex(password)).equals(userPassword)) {
            throw new UserException("密码输入错误！");
        }

        return userEntity;
    }

}