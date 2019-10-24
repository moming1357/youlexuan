package com.youlexuan.manager.controller;

import com.ujiuye.entity.PageResult;
import com.ujiuye.entity.Result;
import com.ujiuye.pojo.TbBrand;
import com.youlexuan.sellergoods.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /*查询所有数据*/
    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    /*分页查询所有数据*/
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand, int page, int rows) {
        return brandService.findPage(brand, page, rows);
    }

    /*添加或者更新数据*/
    @RequestMapping("/addBrand")
    public Result addBrand(@RequestBody TbBrand brand) {
        Result result = new Result();
        boolean b = brandService.addBrand(brand);
        if (b) {
            result.setFalg(b);
            result.setMsg("添加成功");
        } else {
            result.setFalg(b);
            result.setMsg("添加失败");
        }
        return result;
    }

    /*根据id查询一个brand*/
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id) {
        TbBrand brand = brandService.findOne(id);
        return brand;
    }

    /*批量删除*/
    @RequestMapping("batchdelete")
    public Result batchdelete(Long[] ids) {
        Result result = new Result();
        boolean b = brandService.batchdelete(ids);
        if (b) {
            result.setFalg(b);
            result.setMsg("删除成功");
        } else {
            result.setFalg(b);
            result.setMsg("删除失败");
        }
        return result;
    }

    /*查询所有品牌并且是id和text存放到map中的形式*/
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }


}
