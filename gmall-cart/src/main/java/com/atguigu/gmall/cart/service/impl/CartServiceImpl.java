package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.entity.UserInfo;
import com.atguigu.gmall.cart.feign.PmsFeignClient;
import com.atguigu.gmall.cart.feign.SmsFeignClient;
import com.atguigu.gmall.cart.feign.WmsFeignClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.entity.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    @Resource
    private CartMapper cartMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PmsFeignClient pmsFeignClient;
    @Autowired
    private SmsFeignClient smsFeignClient;
    @Autowired
    private WmsFeignClient wmsFeignClient;
    @Autowired
    private CartAsyncService cartAsyncService;

    /**
     * 获取登录信息
     *
     * @return
     */
    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        if (userInfo.getUserId() != null) {
            return userInfo.getUserId().toString();
        }
        return userInfo.getUserKey();
    }

    @Override
    public void addToCart(Cart cart) {

        //1.获取用户信息
        String userId = this.getUserId();
        String key = this.KEY_PREFIX + userId;

        //2.从redis 中获取用户购物车信息
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        Long skuId = cart.getSkuId();
        BigDecimal count = cart.getCount();
        //3.判断用户购物车中是否已含有该商品
        if (hashOps.hasKey(skuId.toString())) {
            //若用户购物车中已存在该商品，则累加数量
            String cartJson = (String) hashOps.get(skuId.toString());
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            //更新数据库
            cartAsyncService.updateByUserIdAndSkuId(userId,cart);
        } else {
            //若用户购物车中不存在该商品，新增购物车记录
            SkuEntity skuEntity = pmsFeignClient.querySkuById(skuId).getData();
            if (skuEntity != null) {
                cart.setUserId(userId);
                cart.setDefaultImage(skuEntity.getDefaultImage());
                cart.setPrice(skuEntity.getPrice());
                cart.setTitle(skuEntity.getTitle());
                cart.setCheck(true);
            }

            //设置销售属性
            List<SkuAttrValueEntity> skuAttrs = pmsFeignClient.querySkuAttrValueEntityBySkuId(skuId).getData();
            String saleAttrs = JSON.toJSONString(skuAttrs);
            cart.setSaleAttrs(saleAttrs);

            //设置营销信息
            List<ItemSaleVo> itemSaleVos = smsFeignClient.querySalesBySkuId(skuId).getData();
            String sales = JSON.toJSONString(itemSaleVos);
            cart.setSales(sales);

            //设置库存
            List<WareSkuEntity> wareSkuEntities = wmsFeignClient.queryWareBySkuId(skuId).getData();

            if (wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)) {
                cart.setStore(true);
            }
            //更新数据库
            cartAsyncService.insertCart(userId,cart);
            //缓存实时价格
            redisTemplate.opsForValue().set(PRICE_PREFIX + cart.getSkuId(),cart.getPrice().toString());
        }
        hashOps.put(skuId.toString(), JSON.toJSONString(cart));
    }

    @Override
    public Cart toCart(Long skuId) {
        // 1.获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        // 2.获取redis 中该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = (String) hashOps.get(skuId.toString());
            Cart cart = JSON.parseObject(cartJson, Cart.class);
            return cart;
        }
        throw new RuntimeException("您的购物车中没有该商品记录！");
    }

    @Override
    public List<Cart> queryCartByUserId() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        Long userId = userInfo.getUserId();

        String unLoginKey = KEY_PREFIX + userKey;
        String loginKey = KEY_PREFIX + userId;

        //1.获取userKey 在redis 中的json 格式的购物车记录集合
        BoundHashOperations<String, Object, Object> unLoginHashOps = redisTemplate.boundHashOps(unLoginKey);
        List<Cart> unLoginCarts = null;

        //2.若userKey 的购物车不为空，则解析获得购物车记录集合
        if (!CollectionUtils.isEmpty(unLoginHashOps.values())) {
            List<Object> cartJsons = unLoginHashOps.values();
            unLoginCarts = cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);

                //查询实时价格设置给购物车记录
                String curPriceStr = redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(curPriceStr));

                return cart;
            }).collect(Collectors.toList());
        }

        //3.若用户未登录（userId 为空），则直接返回userKey 的购物车记录集合
        if (userId == null) {
            return unLoginCarts;
        }

        //4.若用户已登录，但不存在登录时的购物车，则返回未登录时购物车
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(loginKey);

        //5.若用户已登录，且存在未登录时的购物车，则合并购物车
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                //若登录时购物车内已有该商品记录，则增加其数量,并异步更新数据库
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    BigDecimal count = cart.getCount();
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();

                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    cartAsyncService.updateByUserIdAndSkuId(userId.toString(),cart);
                }else {
                    //若登录时购物车内没有该商品记录，则新增购物车记录，并异步新增数据库
                    cart.setUserId(userId.toString());
                    cartAsyncService.insertCart(userId.toString(),cart);
                }
                //将修改后的购物车记录转化为json 字符串，同步保存到redis 的登录时购物车中
                loginHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
            });
        }

        //6.删除未登录购物车
        cartAsyncService.delete(userKey);
        redisTemplate.delete(unLoginKey);

        //7.查询所有登录后购物车购物记录（用户已登录且不存在未登录时购物车也可）
        List<Object> cartJsons = loginHashOps.values();
        List<Cart> carts = null;

        if (!CollectionUtils.isEmpty(cartJsons)) {
            carts = cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);

                //查询实时价格设置给购物车记录
                String curPriceStr = redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(curPriceStr));

                return cart;
            }).collect(Collectors.toList());
        }

        return carts;
    }

    @Override
    public void updateCartBySkuId(Cart cart) {
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;
        BigDecimal count = cart.getCount();
        Long skuId = cart.getSkuId();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            //更新到redis 和 mysql
            hashOps.put(skuId.toString(),JSON.toJSONString(cart));
            cartAsyncService.updateByUserIdAndSkuId(userId,cart);
        }
    }

    @Override
    public void deleteCartBySkuId(Long skuId) {
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())) {
            hashOps.delete(skuId.toString());
            cartAsyncService.deleteCartByUserIdAndSkuId(userId,skuId);
        }
    }

    /**
     * 根据userId 获取用户选中的购物车记录
     * @param userId
     * @return
     */
    @Override
    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        List<Object> cartJsons = hashOps.values();

        if (CollectionUtils.isEmpty(cartJsons)) {
            return null;
        }

        return cartJsons.stream().map(cartJson -> {
            Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
            return cart;
        }).filter(Cart::getCheck).collect(Collectors.toList());
    }
}
