package com.youlexuan.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ujiuye.entity.PageResult;
import com.ujiuye.mapper.TbSeckillGoodsMapper;
import com.ujiuye.mapper.TbSeckillOrderMapper;
import com.ujiuye.pojo.TbSeckillGoods;
import com.ujiuye.pojo.TbSeckillOrder;
import com.ujiuye.pojo.TbSeckillOrderExample;
import com.ujiuye.pojo.TbSeckillOrderExample.Criteria;
import com.youlexuan.seckill.service.SeckillOrderService;
import com.youlexuan.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }
        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 提交秒杀商品订单
     *
     * @param seckillId 秒杀商品主键
     * @param userId    用户id
     */
    @Override
    public void submitOrder(Long seckillId, String userId) {
        /*根据秒杀商品id从redis中查询商品*/
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        //判断缓存中是否存在该商品
        if (seckillGoods == null) {//如果不存在
            throw new RuntimeException("商品异常");
        }
        /*判断商品的库存剩余量*/
        if (seckillGoods.getStockCount() == 0) {//库存量为0
            throw new RuntimeException("该商品已售罄");
        }
        /*如果存在并且有库存,则对库存量进行操作*/
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        /*重新放回redis中*/
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
        /*判断操作后的新库存量*/
        if (seckillGoods.getStockCount() == 0) {//如果没库存了，则从redis中删除即可
            /*同步到数据库   */
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            /*从缓存中删除*/
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
        }

        /*将刚创建尚未支付的订单存到redis中*/
        //订单id
        long orderId = idWorker.nextId();
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(orderId);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setUserId(userId);//设置用户ID
        seckillOrder.setStatus("0");//状态  尚未支付
        /*存到缓存中*/
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
    }

    /**
     * 根据用户名查询存在redis中的订单信息
     *
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    /**
     * 支付成功保存订单
     *
     * @param userId
     * @param orderId
     * @param transactionId
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        System.out.println(userId + "====" + orderId + "===" + transactionId);
        /*根据用户id查询订单*/
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        //如果与传递过来的订单号不符
        if (seckillOrder.getId().longValue() != orderId.longValue()) {
            throw new RuntimeException("订单不相符");
        }
        seckillOrder.setTransactionId(transactionId);//支付宝交易流水号
        seckillOrder.setPayTime(new Date());//支付时间
        seckillOrder.setStatus("1");//状态  已支付
        seckillOrderMapper.insert(seckillOrder);//保存到数据库
        /*保存到数据库成功后删除redis中的本条已经完成支付的数据*/
        redisTemplate.boundHashOps("seckillOrder").delete(userId);

    }

    /**
     * 当过了指定时间仍无人付款,删除存在redis中对应的该条订单数据
     * 并且还原(添加)库存量
     *
     * @param userId  用户id
     * @param orderId 订单id
     */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        /*根据用户id获取存到redis中的order信息*/
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
            redisTemplate.boundHashOps("seckillOrder").delete(userId);//删除该条订单信息
        }
        /*还原库存量*/
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
        /*因为之前的业务逻辑是如果库存量为0时，删除了redis中的秒杀商品，并且同步了数据库中的数据*/
        if (seckillGoods != null) {//如果当前库存量不为0
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            /*重新将将新的数据存到redis中*/
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
        }
        /*TODO 还有恰好库存为0时的还原操作思路
         * 1、修改数据库中的数据
         * 2、从数据库查询出修改之后的该条数据，存到redis中
         **/
       /* TbSeckillGoods seckillGoods1 = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
        seckillGoods.setStockCount(1);
        seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods1);
        *//*2、*//*
        redisTemplate.boundHashOps("seckillGoods").put(seckillGoods1.getId(), seckillGoods1);*/


    }
}