package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "store:lock:";
    public static final String LOCK_PREFIX = "wms:lock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLockWare(List<SkuLockVo> skuLockVos) {

        if (CollectionUtils.isEmpty(skuLockVos)) {
            return null;
        }
        //检验数据库并锁库存，设置商品锁定状态
        skuLockVos.forEach(skuLockVo -> checkLock(skuLockVo));

        List<SkuLockVo> successLockVos = skuLockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
        List<SkuLockVo> failLockVos = skuLockVos.stream().filter(skuLockVo -> !skuLockVo.getLock()).collect(Collectors.toList());

        //若存在锁库失败的商品，将锁库成功的商品解锁
        if (!CollectionUtils.isEmpty(failLockVos)) {
            successLockVos.forEach(skuLockVo -> baseMapper.tryUnLock(skuLockVo.getWareSkuId(),skuLockVo.getCount()));
            return skuLockVos;
        }

        String skuLockVosJson = JSON.toJSONString(skuLockVos);
        template.opsForValue().set(KEY_PREFIX + skuLockVos.get(0).getOrderToken(),skuLockVosJson);

        //定时解锁库存
        rabbitTemplate.convertAndSend("ORDER-EXCHANGE","stock.unlock",skuLockVos.get(0).getOrderToken());

        //都锁定成功不需要展示锁定成功
        return null;
    }

    private void checkLock(SkuLockVo skuLockVo) {
        //加公平锁
        RLock fairLock = redissonClient.getFairLock(LOCK_PREFIX + skuLockVo.getOrderToken());
        fairLock.lock();

        //查询库存满足条件的仓库
        List<WareSkuEntity> wareSkuEntities = baseMapper.checkSkuStock(skuLockVo.getSkuId(),skuLockVo.getCount());

        //若没有库存充足的仓库，尝试锁库失败
        if (CollectionUtils.isEmpty(wareSkuEntities)) {
            skuLockVo.setLock(false);
            fairLock.unlock();
            return;
        }

        //若有,则从第一个仓库发货，锁库存
        Long wareSkuId = wareSkuEntities.get(0).getId();
        if (baseMapper.tryLock(wareSkuId,skuLockVo.getCount()) == 1) {
            skuLockVo.setLock(true);
            skuLockVo.setWareSkuId(wareSkuId);
        }else {
            skuLockVo.setLock(false);
        }

        fairLock.unlock();
    }

}