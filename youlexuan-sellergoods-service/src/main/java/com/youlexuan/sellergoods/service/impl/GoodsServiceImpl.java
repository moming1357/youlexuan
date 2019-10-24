package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ujiuye.entity.PageResult;
import com.ujiuye.mapper.*;
import com.ujiuye.pojo.*;
import com.ujiuye.pojo.TbGoodsExample.Criteria;
import com.ujiuye.pojogroup.Goods;
import com.youlexuan.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
//    @Override
//    @Transactional
//    public void add(Goods goods) {
//        goods.getTbGoods().setAuditStatus("0");//设置未申请状态
//        System.out.println(goods.getTbGoods().getBrandId());
//        goodsMapper.insert(goods.getTbGoods());
//        goods.getTbGoodsDesc().setGoodsId(goods.getTbGoods().getId());//设置ID
//        goodsDescMapper.insert(goods.getTbGoodsDesc());//插入商品扩展数据
//
//        for (TbItem item : goods.getItemList()) {
//            //标题
//            String title = goods.getTbGoods().getGoodsName();
//            Map<String, Object> specMap = JSON.parseObject(item.getSpec());
//            for (String key : specMap.keySet()) {
//                title += " " + specMap.get(key);
//            }
//            item.setTitle(title);
//            item.setGoodsId(goods.getTbGoods().getId());//商品SPU编号
//            item.setSellerId(goods.getTbGoods().getSellerId());//商家编号
//            item.setCategoryid(goods.getTbGoods().getCategory3Id());//商品分类编号（3级）
//            item.setCreateTime(new Date());//创建日期
//            item.setUpdateTime(new Date());//修改日期
//            //品牌名称
//            TbBrand brand = brandMapper.selectByPrimaryKey(goods.getTbGoods().getBrandId());
//            System.out.println(brand);
//            item.setBrand(brand.getName());
//            //分类名称
//            TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getTbGoods().getCategory3Id());
//            item.setCategory(itemCat.getName());
//            //商家名称
//            TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getTbGoods().getSellerId());
//            item.setSeller(seller.getNickName());
//            //图片地址（取spu的第一个图片）
//            List<Map> imageList = JSON.parseArray(goods.getTbGoodsDesc().getItemImages(), Map.class);
//            if (imageList.size() > 0) {
//                item.setImage((String) imageList.get(0).get("url"));
//            }
//            itemMapper.insert(item);
//            System.out.println("000000000000000000000000");
//        }
//    }
    @Override
    public void add(Goods goods) {
        goods.getTbGoods().setAuditStatus("0");
        goodsMapper.insertSelective(goods.getTbGoods());    //插入商品表
        goods.getTbGoodsDesc().setGoodsId(goods.getTbGoods().getId());
        goodsDescMapper.insertSelective(goods.getTbGoodsDesc());//插入商品扩展数据
        saveItemList(goods);//插入商品SKU列表数据
//        if ("1".equals(goods.getTbGoods().getIsEnableSpec())) {
//            for (TbItem item : goods.getItemList()) {
//                //标题
//                String title = goods.getTbGoods().getGoodsName();
//                Map<String, Object> specMap = JSON.parseObject(item.getSpec());
//                for (String key : specMap.keySet()) {
//                    title += " " + specMap.get(key);
//                }
//                item.setTitle(title);
//                setItemValus(goods, item);
//                itemMapper.insertSelective(item);
//            }
//        } else {
//            TbItem item = new TbItem();
//            item.setTitle(goods.getTbGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
//            item.setPrice(goods.getTbGoods().getPrice());//价格
//            item.setStatus("1");//状态
//            item.setIsDefault("1");//是否默认
//            item.setNum(9999);//库存数量
//            item.setSpec("{}");
//            setItemValus(goods, item);
//            itemMapper.insertSelective(item);
//        }
    }

    /**
     * 插入SKU列表数据
     *
     * @param goods
     */
    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getTbGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                //标题
                String title = goods.getTbGoods().getGoodsName();
                Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                for (String key : specMap.keySet()) {
                    title += " " + specMap.get(key);
                }
                item.setTitle(title);
                setItemValus(goods, item);
                itemMapper.insertSelective(item);
            }
        } else {
            TbItem item = new TbItem();
            item.setTitle(goods.getTbGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
            item.setPrice(goods.getTbGoods().getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(9999);//库存数量
            item.setSpec("{}");
            setItemValus(goods, item);
            itemMapper.insertSelective(item);
        }
    }

    private void setItemValus(Goods goods, TbItem item) {
        item.setGoodsId(goods.getTbGoods().getId());//商品SPU编号
        item.setSellerId(goods.getTbGoods().getSellerId());//商家编号
        item.setCategoryid(goods.getTbGoods().getCategory3Id());//商品分类编号（3级）
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//修改日期

        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getTbGoods().getBrandId());
        item.setBrand(brand.getName());
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getTbGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getTbGoods().getSellerId());
        item.setSeller(seller.getNickName());

        //图片地址（取spu的第一个图片）
        List<Map> imageList = JSON.parseArray(goods.getTbGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        goods.getTbGoods().setAuditStatus("0");//设置未申请状态:如果是经过修改的商品，需要重新设置状态
        goodsMapper.updateByPrimaryKey(goods.getTbGoods());//保存商品表
        goodsDescMapper.updateByPrimaryKey(goods.getTbGoodsDesc());//保存商品扩展表
        //删除原有的sku列表数据
        TbItemExample example = new TbItemExample();
        com.ujiuye.pojo.TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getTbGoods().getId());
        itemMapper.deleteByExample(example);
        //添加新的sku列表数据
        saveItemList(goods);//插入商品SKU列表数据
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setTbGoods(tbGoods);
        goods.setTbGoodsDesc(tbGoodsDesc);

        //查询SKU商品列表
        TbItemExample example = new TbItemExample();
        com.ujiuye.pojo.TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);//查询条件：商品ID
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
//                criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }
        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setAuditStatus(status);
            System.out.println(goods.getAuditStatus());
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

    @Override
    public void updateUpOrDown(String isMarketable, Long id) {
        TbGoods tbGoods = new TbGoods();
        tbGoods.setId(id);
        tbGoods.setIsMarketable(isMarketable);
        goodsMapper.updateByPrimaryKeySelective(tbGoods);
    }

    /*根据主键以及status查询所有的item*/
    @Override
    public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
        TbItemExample example = new TbItemExample();
        com.ujiuye.pojo.TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        criteria.andStatusEqualTo(status);
        return itemMapper.selectByExample(example);
    }
}
