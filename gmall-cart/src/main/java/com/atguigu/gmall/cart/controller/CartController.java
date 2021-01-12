package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 新增商品到购物车
     * @param cart
     * @return
     */
    @GetMapping
    public String addToCart(Cart cart) {
        cartService.addToCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart.html")
    public String toCart(Long skuId, Model model) {
        Cart cart = cartService.toCart(skuId);
        model.addAttribute("cart",cart);
        return "addCart";
    }

    /**
     * 查询购物车
     * @param model
     * @return
     */
    @GetMapping("cart.html")
    public String queryCartByUserId(Model model) {

        List<Cart> carts = cartService.queryCartByUserId();
        model.addAttribute("carts",carts);

        return "cart";
    }

    @ResponseBody
    @PostMapping("updateNum")
    public ResponseVo<Object> updateCartBySkuId(@RequestBody Cart cart) {

        cartService.updateCartBySkuId(cart);

        return ResponseVo.ok();
    }

    @ResponseBody
    @PostMapping("deleteCart")
    public void deleteCartBySkuId(@RequestParam(value = "skuId") Long skuId) {

        cartService.deleteCartBySkuId(skuId);
    }


    @ResponseBody
    @GetMapping("check/{userId}")
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable(value = "userId")Long userId) {
        List<Cart> carts = cartService.queryCheckedCartsByUserId(userId);
        return ResponseVo.ok(carts);
    }
}
