package com.heiying.heiyingmail.cart.controller;

import com.alibaba.fastjson.JSON;
import com.heiying.heiyingmail.cart.service.CartService;
import com.heiying.heiyingmail.cart.vo.Cart;
import com.heiying.heiyingmail.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {
    @Autowired
    CartService cartService;


    @GetMapping("/currentUserItems")
    @ResponseBody
    public List<CartItem> getCurrentUserItems(){
        return cartService.getCurrentUserItems();
    }



    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("deleteId") Long deleteId
    ) {
        cartService.deleteItem(deleteId);
        return "redirect:http://cart.heiyingmail.com/cart.html";
    }


    @GetMapping("/countItem")
    @ResponseBody
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) throws ExecutionException, InterruptedException {
        cartService.changeItemCount(skuId, num);
        Cart cart = cartService.getCart();
        return JSON.toJSONString(cart);
    }


    @GetMapping("/checkItem")
    @ResponseBody
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("checked") Integer checked) throws ExecutionException, InterruptedException {
        cartService.checkItem(skuId, checked);
        Cart cart = cartService.getCart();
        return JSON.toJSONString(cart);
    }

    /**
     * 浏览器有一个cookie：user-key：标识用户身份，一个月过期；
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器以后保存，都会带上这个cookie
     * <p>
     * 登录：session有
     * 没登陆：按照cookie里面带来的user-key来做
     * 第一次：如果没有临时用户，自动创建一个临时用户
     *
     * @param
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
//        UserInfoTO userInfoTO = CartInterceptor.threadLocal.get();
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        //登录了获取登录的购物车数据
        return "cartList";

        //没登陆获取临时购物车数据

    }

    /**
     * 添加商品到购物车
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num, Model model) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addToCart(skuId, num);
        model.addAttribute("cartItem", cartItem);
        return "redirect:http://cart.heiyingmail.com/addToCartSuccess.html?skuId=" + skuId;
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartPageSuccess(@RequestParam("skuId") Long skuId, Model model) {
        //重定向到成功页面。再次查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }
}
