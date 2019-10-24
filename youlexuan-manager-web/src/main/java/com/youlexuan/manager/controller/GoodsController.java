package com.youlexuan.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ujiuye.entity.PageResult;
import com.ujiuye.entity.Result;
import com.ujiuye.pojo.TbGoods;
import com.ujiuye.pojo.TbItem;
import com.ujiuye.pojogroup.Goods;
import com.youlexuan.page.service.ItemPageService;
import com.youlexuan.search.service.ItemSearchService;
import com.youlexuan.sellergoods.service.GoodsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    @Reference
    private ItemSearchService itemSearchService;
    @Reference(timeout = 40000)
    private ItemPageService itemPageService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            /*删除存在solr索引库中的某些条数据*/
            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /*更新商品审核状态*/
    @RequestMapping("/updateStatus")
    public Result delete(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);

            //按照SPU ID查询 SKU列表(状态为1)
            if (status.equals("1")) {//审核通过
                List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
                //调用搜索接口实现数据批量导入solr索引库
                if (itemList.size() > 0) {
                    /*删除solr索引库中已经存在的之前审核过的商品*/
                    itemSearchService.deleteSolrById(ids);
                    //导入新的数据到solr索引库中
                    /*问题；由于涉及io流操作,所以导致前台页面的审核响应速度慢,用户不好的体验*/
                    itemSearchService.importList(itemList);
                } else {
                    System.out.println("没有明细数据");
                }
                /*问题同上;涉及io流，使响应的时间大大延长*/
                //静态页生成
                for (Long goodsId : ids) {
                    itemPageService.genItemHtml(goodsId);
                }
                /*解决方案；使用消息中间组件（ActiveMQ）进行监听以及操作*/
            }
            return new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }
    }

    /**
     * 生成静态页（测试）
     *
     * @param goodsId
     */
    @RequestMapping("/genHtml")
    public void genHtml(Long goodsId) {
        itemPageService.genItemHtml(goodsId);
    }

}
