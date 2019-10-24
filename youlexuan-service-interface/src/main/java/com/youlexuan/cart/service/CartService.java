package com.youlexuan.cart.service;

import com.ujiuye.extpojo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /*从redis中查询购物车列表*/
    public List<Cart> findCartListFromRedis(String username);

    /*将购物车列表保存到redis中*/
    public void saveCartListToRedis(String username, List<Cart> cartList);

    // 合并购物车
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
