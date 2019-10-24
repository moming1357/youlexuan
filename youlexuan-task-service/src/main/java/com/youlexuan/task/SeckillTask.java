package com.youlexuan.task;

import com.ujiuye.mapper.TbSeckillGoodsMapper;
import com.ujiuye.pojo.TbSeckillGoods;
import com.ujiuye.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/*springTask可以用来做计划任务
 * 例如 定期查询数据库的数据进行 库存预警   保质期预警*/
@Component
public class SeckillTask {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 刷新秒杀商品
     * <p>
     * 知识点cron表达式
     * 有两种格式
     * （1）Seconds Minutes Hours DayofMonth Month DayofWeek Year 秒，分，时，天，月，星期，年
     * （2）Seconds Minutes Hours DayofMonth Month DayofWeek 秒，分，时，天，月，星期
     */
    @Scheduled(cron = "* * * * * ?")
    public void refreshSeckillGoods() {
        Set ids = redisTemplate.boundHashOps("seckillGoods").keys();
        System.out.println("========" + ids + "\t" + ids.toString());
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        /*通过审核的*/
        criteria.andStatusEqualTo("1");
        /*在范围内的*/
        criteria.andStartTimeLessThan(new Date());
        criteria.andEndTimeGreaterThan(new Date());
        /*库存不为0*/
        criteria.andStockCountGreaterThan(0);
        if (ids != null && ids.size() > 0) {
            ArrayList list = new ArrayList();
            list.addAll(ids);
            criteria.andIdNotIn(list);//排除缓存中已经存在的数据
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
            }
            System.out.println("将" + seckillGoodsList.size() + "条商品装入缓存");
        }
    }

    /**
     * 移除过期的秒杀商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                /*获取秒杀结束时间*/
                if (seckillGoods.getEndTime().before(new Date())) {//证明已经过了秒杀时间
                    /*持久化到数据库*/
                    seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//向数据库保存记录
                    /*从redis中移除该条数据*/
                    redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
                    System.out.println("移除秒杀商品" + seckillGoods.getId());
                }
            }
        }
    }
}
