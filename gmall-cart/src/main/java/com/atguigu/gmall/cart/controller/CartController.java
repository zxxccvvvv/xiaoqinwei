package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
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
    CartService cartService;

    //根据用户Id查询选中状态
    @GetMapping("user/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId")Long userId){
        List<Cart> cartList = cartService.queryCheckedCartsByUserId(userId);

        System.out.println("test分支第一次提交");
        System.out.println("test分支第二次提交");

        return ResponseVo.ok(cartList);

    }

    //更改选中状态
    @PostMapping("updateStatus")
    @ResponseBody
    public ResponseVo updateStatus(@RequestBody Cart cart){
        cartService.updateStatus(cart);
        return ResponseVo.ok();
    }

    //新增购物车，新增成功后重定向新增购物车成功页面
    @GetMapping()
    public String addCart(Cart cart){
        if (cart == null || cart.getSkuId() == null){
            throw new RuntimeException("没有选择添加到购物车的商品信息");

        }
        cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();

    }

    //新增成功购物车页面，本质就是根据用户登录信息和skuId查询新增购物车
    @GetMapping("addCart.html")
    public String queryCart(@RequestParam("skuId")Long skuId, Model model){
        Cart cart = cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    //查询购物车列表
    @GetMapping("cart.html")
    public String queryCarts(Model model){
        List<Cart> carts =  cartService.queryCarts();
        model.addAttribute("carts",carts);
        return "cart";

    }

    //更新购物车
    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){
        cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }


    @GetMapping("test")
    @ResponseBody
    public String test(){
        System.out.println(LoginInterceptor.getUserInfo());
        return "hello cart";
    }

}
