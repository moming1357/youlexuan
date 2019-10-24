package com.youlexuan.seckill.service;

import com.ujiuye.entity.PageResult;
import com.ujiuye.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 服务层接口
 *
 * @author Administrator
 */
public interface SeckillOrderService {

    /**
     * 返回全部列表
     *
     * @return
     */
    public List<TbSeckillOrder> findAll();


    /**
     * 返回分页列表
     *
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize);


    /**
     * 增加
     */
    public void add(TbSeckillOrder seckill_order);


    /**
     * 修改
     */
    public void update(TbSeckillOrder seckill_order);


    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    public TbSeckillOrder findOne(Long id);


    /**
     * 批量删除
     *
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 分页
     *
     * @param pageNum  当前页 码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(TbSeckillOrder seckill_order, int pageNum, int pageSize);

    /**
     * 提交秒杀商品订单
     *
     * @param seckillId 秒杀商品主键
     * @param userId    用户id
     */
    public void submitOrder(Long seckillId, String userId);

    /**
     * 根据用户名查询秒杀订单
     *
     * @param userId
     */
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId);

    /**
     * 支付成功保存订单
     *
     * @param userId
     * @param orderId
     */
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId);


    /**
     * 当过了指定时间仍无人付款,删除存在redis中对应的该条订单数据
     *
     * @param userId 用户id
     * @param orderId 订单id
     */
    public void deleteOrderFromRedis(String userId, Long orderId);
}
