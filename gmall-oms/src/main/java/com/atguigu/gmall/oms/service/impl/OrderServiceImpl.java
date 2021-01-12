package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.PmsFeignClient;
import com.atguigu.gmall.oms.feign.SmsFeignClient;
import com.atguigu.gmall.oms.feign.WmsFeignClient;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.oms.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSumbitVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.sms.entity.ItemSaleVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.smartcardio.CardTerminal;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Resource
    private PmsFeignClient pmsFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public OrderEntity saveOrder(OrderSumbitVo orderSumbitVo, Long userId) {
        //向数据库中插入订单信息
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(orderSumbitVo, orderEntity);
        orderEntity.setOrderSn(orderSumbitVo.getOrderToken());
        orderEntity.setTotalAmount(orderSumbitVo.getTotalPrice());

        orderEntity.setSourceType(0);
        orderEntity.setReceiverName(orderSumbitVo.getAddress().getName());
        orderEntity.setReceiverCity(orderSumbitVo.getAddress().getCity());
        orderEntity.setReceiverPhone(orderSumbitVo.getAddress().getPhone());
        orderEntity.setReceiverProvince(orderSumbitVo.getAddress().getProvince());
        orderEntity.setReceiverPostCode(orderSumbitVo.getAddress().getPostCode());
        orderEntity.setReceiverRegion(orderSumbitVo.getAddress().getRegion());
        orderEntity.setReceiverAddress(orderSumbitVo.getAddress().getAddress());
        orderEntity.setCreateTime(new Date());

        baseMapper.insert(orderEntity);

        Long orderEntityId = orderEntity.getId();

        //向数据库中插入订单详情信息
        List<OrderItemVo> items = orderSumbitVo.getItems();
        items.forEach(orderItemVo -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            Long skuId = orderItemVo.getSkuId();

            orderItemEntity.setOrderId(orderEntityId);
            orderItemEntity.setOrderSn(orderSumbitVo.getOrderToken());

            orderItemEntity.setSkuQuantity(orderItemVo.getCount().intValue());
            orderItemEntity.setSkuAttrsVals(JSON.toJSONString(orderItemVo.getSaleAttrs()));

            CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                SkuEntity skuEntity = pmsFeignClient.querySkuById(skuId).getData();
                if (skuEntity == null) {
                    return;
                }

                //为订单详情设置sku 相关信息
                orderItemEntity.setSkuId(skuId);
                orderItemEntity.setCategoryId(skuEntity.getCatagoryId());
                orderItemEntity.setSkuName(skuEntity.getName());
                orderItemEntity.setSkuPrice(skuEntity.getPrice());
                orderItemEntity.setSkuPic(skuEntity.getDefaultImage());

                CompletableFuture<Void> brandCompletableFuture = CompletableFuture.runAsync(() -> {
                    //为订单详情设置品牌名称
                    BrandEntity brandEntity = pmsFeignClient.queryBrandById(skuEntity.getBrandId()).getData();
                    orderItemEntity.setSpuBrand(brandEntity.getName());
                }, threadPoolExecutor);

                CompletableFuture<Void> spuCompletableFuture = CompletableFuture.runAsync(() -> {
                    //为订单详情设置spu 相关信息
                    SpuEntity spuEntity = pmsFeignClient.querySpuById(skuEntity.getSpuId()).getData();
                    orderItemEntity.setSpuId(skuEntity.getSpuId());
                    orderItemEntity.setSpuName(spuEntity.getName());
                }, threadPoolExecutor);

                CompletableFuture<Void> spuDescCompletableFuture = CompletableFuture.runAsync(() -> {
                    SpuDescEntity spuDescEntity = pmsFeignClient.querySpuDescById(skuEntity.getSpuId()).getData();
                    //为订单详情设置spu 描述相关信息
                    if (spuDescEntity != null) {
                        orderItemEntity.setSpuPic(spuDescEntity.getDecript());
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> skuAttrsCompletableFuture = CompletableFuture.runAsync(() -> {
                    List<SkuAttrValueEntity> skuAttrValueEntities = pmsFeignClient.querySkuAttrValueEntityBySkuId(skuId).getData();
                    //为订单详情设置sku 属性参数集合
                    if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                        orderItemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValueEntities));
                    }
                }, threadPoolExecutor);

                CompletableFuture.allOf(brandCompletableFuture,spuCompletableFuture,spuDescCompletableFuture,skuAttrsCompletableFuture).join();
            }, threadPoolExecutor);

            CompletableFuture.allOf(skuCompletableFuture).join();

            orderItemMapper.insert(orderItemEntity);
        });

        rabbitTemplate.convertAndSend("ORDER-EXCHANGE","order.create",orderSumbitVo.getOrderToken());
        return orderEntity;
    }

}