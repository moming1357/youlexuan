package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.ujiuye.entity.Result;
import com.ujiuye.extpojo.Cart;
import com.youlexuan.cart.service.CartService;
import com.youlexuan.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 6000000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 获取购物车列表
     *
     * @param
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //得到登陆人账号,判断当前是否有人登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if ("anonymousUser".equals(username)) {//说明没有登录,所以从cookie中取数据
            /*没有登录的情况下从cookie中查询购物车列表*/
            System.out.println("从cookie中取值");
            return cartList_cookie;
        } else {
            System.out.println("从redis中取值");
            /*登录的情况下从redis去数据*/
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size() > 0) {//如果本地存在购物车
                //合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
            }
            return cartList_redis;
        }


    }

    /*添加商品到购物车*/
//   springmvc4.2以及以上版本可以使用注解允许跨域
// @CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")
    @RequestMapping("addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        /*解决跨域问题 允许9105访问*/
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        //得到登录人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        try {
            /*获取购物车列表*/
            List<Cart> cartList = findCartList();
            List<Cart> cart = cartService.addGoodsToCartList(cartList, itemId, num);

            if ("anonymousUser".equals(username)) {//说明没有登录，则将数据存到cookie中
                /*添加购物车列表到缓存中*/
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cart), 3600 * 24 * 3, "UTF-8");
                System.out.println("向cookie存入数据");
            } else {
                /*将数据存到redis中*/
                cartService.saveCartListToRedis(username, cart);

                System.out.println("向redis中存入数据");
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

}
