package com.youlexuan.sellergoods.service;

import com.ujiuye.entity.PageResult;
import com.ujiuye.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /*查询所有的品牌管理信息*/
    public List<TbBrand> findAll();

    /*分页查询品牌信息*/
    public PageResult findPage(TbBrand brand,int pageNum, int pageSize);

    boolean addBrand(TbBrand brand);

    TbBrand findOne(Long id);

    boolean batchdelete(Long[] ids);

    List<Map> selectOptionList();
}
