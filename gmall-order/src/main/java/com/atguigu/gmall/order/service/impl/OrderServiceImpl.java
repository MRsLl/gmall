package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.entity.UserInfo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.exception.OrderException;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSumbitVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.entity.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String KEY_PREFIX = "order:token:";

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OmsFeignClient omsFeignClient;
    @Autowired
    private PmsFeignClient pmsFeignClient;
    @Autowired
    private WmsFeignClient wmsFeignClient;
    @Autowired
    private SmsFeignClient smsFeignClient;
    @Autowired
    private UmsFeignClient umsFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;


    @Override
    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        if (userInfo == null) {
            return null;
        }

        Long userId = userInfo.getUserId();

        //1.根据userId 获取用户地址集合
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            List<UserAddressEntity> userAddressEntities = umsFeignClient.queryAddressesByUserId(userId).getData();

            if (!CollectionUtils.isEmpty(userAddressEntities)) {
                confirmVo.setAddresses(userAddressEntities);
            }
        }, threadPoolExecutor);

        //2.根据userId 获取用户选中的购物车记录集合
        CompletableFuture<List<Cart>> cartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            List<Cart> carts = cartFeignClient.queryCheckedCartsByUserId(userId).getData();

            if (CollectionUtils.isEmpty(carts)) {
                throw new OrderException("该用户没有购物车记录");
            }

            return carts;
        }, threadPoolExecutor);

        //3.查出用户所有购物车记录相关信息
        CompletableFuture<Void> itemCompletableFuture = cartCompletableFuture.thenAcceptAsync(carts -> {

            List<OrderItemVo> orderItemVos = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                Long skuId = cart.getSkuId();
                orderItemVo.setSkuId(skuId);
                orderItemVo.setCount(cart.getCount());

                //根据skuId 获取sku 基本信息
                CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                    SkuEntity skuEntity = pmsFeignClient.querySkuById(skuId).getData();
                    BeanUtils.copyProperties(skuEntity, orderItemVo);
                }, threadPoolExecutor);

                //根据skuId 获取库存信息
                CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
                    List<WareSkuEntity> wareSkuEntities = wmsFeignClient.queryWareBySkuId(skuId).getData();

                    if (wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)) {
                        orderItemVo.setStore(true);
                    }
                }, threadPoolExecutor);

                //根据skuId 获取销售属性
                CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                    List<SkuAttrValueEntity> skuAttrValueEntities = pmsFeignClient.querySkuAttrValueEntityBySkuId(skuId).getData();
                    orderItemVo.setSaleAttrs(skuAttrValueEntities);
                }, threadPoolExecutor);

                //根据skuId 获取营销信息
                CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
                    List<ItemSaleVo> itemSaleVos = smsFeignClient.querySalesBySkuId(skuId).getData();
                    orderItemVo.setSales(itemSaleVos);
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuCompletableFuture, storeCompletableFuture, saleAttrCompletableFuture, saleCompletableFuture).join();

                return orderItemVo;
            }).collect(Collectors.toList());

            confirmVo.setItems(orderItemVos);
        }, threadPoolExecutor);

        //4.获取用户积分信息
        CompletableFuture<Void> userCompletableFuture = CompletableFuture.runAsync(() -> {
            UserEntity userEntity = umsFeignClient.queryUserById(userId).getData();

            confirmVo.setBounds(userEntity.getIntegration());
        }, threadPoolExecutor);

        //5..为订单设置唯一标识
        CompletableFuture<Void> tokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String timeId = IdWorker.getTimeId();
            confirmVo.setOrderToken(timeId);
            this.redisTemplate.opsForValue().set(KEY_PREFIX + timeId, timeId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(addressCompletableFuture,userCompletableFuture,cartCompletableFuture,itemCompletableFuture,tokenCompletableFuture).join();

        return confirmVo;
    }

    @Override
    public OrderEntity submitOrder(OrderSumbitVo orderSumbitVo) {
        //1.验重，防止订单重复提交
        String orderToken = orderSumbitVo.getOrderToken();
        String script = "if (redis.call('get',KEYS[1]) == ARG[1])" +
                "then" +
                "redis.call('del',KEYS[1])" +
                "return 1" +
                "else" +
                "return 0" +
                "end";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);

        if (!flag) {
            throw new OrderException("请不要重复提交订单");
        }

        //2.验证价格
        List<OrderItemVo> items = orderSumbitVo.getItems();
        if (CollectionUtils.isEmpty(items)) {
            throw new OrderException("请选中商品后提交订单");
        }

        //从数据库中查出订单商品的实时总价
        BigDecimal curTotalPrice = items.stream().map(item -> {
            SkuEntity skuEntity = pmsFeignClient.querySkuById(item.getSkuId()).getData();

            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(item.getCount());
            }
            return new BigDecimal(0);
        }).reduce((t1, t2) -> t1.add(t2)).get();

        if (orderSumbitVo.getTotalPrice().compareTo(curTotalPrice) != 0) {
            throw new OrderException("商品信息过期，请刷新页面重新提交订单");
        }

        //3.验证库存，并锁定库存
        List<SkuLockVo> skuLockVos = items.stream().map(orderItemVo -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            BeanUtils.copyProperties(orderItemVo, skuLockVo);
            return skuLockVo;
        }).collect(Collectors.toList());

        List<SkuLockVo> skuLockVoList = wmsFeignClient.checkAndLockWare(skuLockVos).getData();

        if (!CollectionUtils.isEmpty(skuLockVoList)) {
            throw new OrderException("手慢了，商品已售完" + JSON.toJSONString(skuLockVoList));
        }

        //4.生成订单
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        OrderEntity orderEntity = null;
        try {
            //若订单创建成功，要定时关单
            orderEntity = omsFeignClient.saveOrder(orderSumbitVo, userId).getData();
        } catch (Exception e) {
            e.printStackTrace();
            //如果生成订单失败，立刻解锁库存
            rabbitTemplate.convertAndSend("ORDER-EXCHANGE","stock.unlock",orderToken);
        }

        //5.删除购物车中相对应的记录（MQ）
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId",userId);
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        map.put("skuIds",JSON.toJSONString(skuIds));

        rabbitTemplate.convertAndSend("ORDER-EXCHANGE","cart.delete",map);

        return orderEntity;
    }
}
