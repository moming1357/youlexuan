package com.youlexuan.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ujiuye.extpojo.Cart;
import com.ujiuye.mapper.TbItemMapper;
import com.ujiuye.mapper.TbOrderItemMapper;
import com.ujiuye.pojo.TbItem;
import com.ujiuye.pojo.TbOrderItem;
import com.youlexuan.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加购物车
     * 1.根据商品SKU ID查询SKU商品信息
     * 2.获取商家ID
     * 3.根据商家ID判断购物车列表中是否存在该商家的购物车
     * 4.如果购物车列表中不存在该商家的购物车
     * 4.1 新建购物车对象
     * 4.2 将新建的购物车对象添加到购物车列表
     * 5.如果购物车列表中存在该商家的购物车
     * 查询购物车明细列表中是否存在该商品
     * 5.1. 如果没有，新增购物车明细
     * 5.2. 如果有，在原购物车明细上添加数量，更改金额
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        /*根据前台传递的itemId获取商家id信息*/
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }
        /*根据商家id判断购物车列表是否有该商家的购物车*/
        String sellerId = item.getSellerId();//获取商家id
        Cart cart = searchCartBySellerId(cartList, sellerId);
        /*若购物车列表中没有该购物车对象，则创建一个*/
        if (cart == null) {
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List orderItemList = new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            /*将购物车对象放到购物车列表中*/
            cartList.add(cart);
        } else {
            /*如果购物车列表中存在该购物车*/
            /*判断该购物车中是否已经有该商品*/
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            /*如果该购物车不存在该商品*/
            if (orderItem == null) {
                /*向该购物车添加该商品*/
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {//如果该购物车中已经有了该商品,则修改商品数量以及其他信息
                orderItem.setNum(orderItem.getNum() + num);//设置商品数量
                /*商品总价格=商品单价*商品数量*/
                double v = orderItem.getPrice().doubleValue();
                int newNum = orderItem.getNum();
                double totalFee = v * newNum;
                orderItem.setTotalFee(new BigDecimal(totalFee));
                /*如果商品数量小于等于0,则移除商品*/
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                /*如果cart购物车对象小于等于0，则移除该购物车对象*/
                if (cartList.size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 根据itemid查询商品
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().equals(itemId)) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车对象
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    /**
     * 根据商家id查询购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /*从redis中获取购物车列表*/
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....." + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartListRedis").get(username);
        System.out.println(cartList);
        if (cartList == null) {
            cartList = new ArrayList();
        }
        return cartList;
    }

    /*将购物车列表存到redis中*/
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("将数据存到redis中....." + username);
        System.out.println("将要存到redis中的数据" + cartList);
        redisTemplate.boundHashOps("cartListRedis").put(username, cartList);
    }

    /*
    将cookie与redis合并购物车列表*/
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }
}
