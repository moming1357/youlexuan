package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ujiuye.entity.PageResult;
import com.ujiuye.entity.Result;
import com.ujiuye.mapper.TbItemCatMapper;
import com.ujiuye.pojo.TbItemCat;
import com.ujiuye.pojo.TbItemCatExample;
import com.ujiuye.pojo.TbItemCatExample.Criteria;
import com.youlexuan.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 商品类目服务实现层
 *
 * @author Administrator
 */
@Service(timeout = 100000000)
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbItemCat> findAll() {
        return itemCatMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbItemCat itemCat) {
        itemCatMapper.insert(itemCat);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbItemCat itemCat) {
        itemCatMapper.updateByPrimaryKey(itemCat);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbItemCat findOne(Long id) {
        return itemCatMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public Result delete(Long[] ids) {
        if (ids.length > 0) {
            for (Long id : ids) {
                TbItemCatExample tbItemCatExample = new TbItemCatExample();
                tbItemCatExample.createCriteria().andParentIdEqualTo(id);
                List<TbItemCat> tbItemCats = itemCatMapper.selectByExample(tbItemCatExample);
                if (tbItemCats.size() > 0) {//证明其下还有子分类
                    return new Result(false, "要想删除该分类，请先删除其下子分类");
                } else {
                    boolean b = itemCatMapper.deleteByPrimaryKey(id) > 0;
                    if (b) {
                        return new Result(true, "删除成功");
                    } else {
                        return new Result(true, "删除失败");
                    }
                }
            }
        }
        return new Result(true, "还没选择删除项");
    }


    @Override
    public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbItemCatExample example = new TbItemCatExample();
        Criteria criteria = example.createCriteria();

        if (itemCat != null) {
            if (itemCat.getName() != null && itemCat.getName().length() > 0) {
                criteria.andNameLike("%" + itemCat.getName() + "%");
            }
        }

        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<TbItemCat> findItemCatListByPid(Long parentId) {
        TbItemCatExample exam = new TbItemCatExample();
        Criteria criteria = exam.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        //每次执行查询的时候，一次性读取缓存进行存储 (因为每次增删改都要执行此方法)
        List<TbItemCat> list = findAll();
        for (TbItemCat itemCat : list) {
            /*redis的itemCat中存放模板id以及分类名称*/
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
        }
        System.out.println("更新缓存:商品分类表");
        return itemCatMapper.selectByExample(exam);
    }
}
