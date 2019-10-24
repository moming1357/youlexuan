package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ujiuye.entity.PageResult;
import com.ujiuye.mapper.TbBrandMapper;
import com.ujiuye.pojo.TbBrand;
import com.ujiuye.pojo.TbBrandExample;
import com.youlexuan.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(TbBrand brand,int pageNum, int pageSize) {
        /*分页*/
        PageHelper.startPage(pageNum, pageSize);
        TbBrandExample example= new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        /*不为空时进行模糊查询*/
        if(brand!=null){
            if(brand.getName()!=null && brand.getName().length()>0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
        }
        /*将查询到的数据存到page中
         * page继承了arraylist*/
        List<TbBrand> tbBrands = brandMapper.selectByExample(example);
        PageInfo<TbBrand> page = new PageInfo<>(tbBrands);
//        PageInfo<TbBrand> page = (PageInfo<TbBrand>) brandMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getList());
    }

    /*添加或者修改*/
    @Override
    public boolean addBrand(TbBrand brand) {
        if (brand.getId() != null) {
            boolean b1 = brandMapper.updateByPrimaryKeySelective(brand) > 0;
            return b1;
        } else {
            boolean b2 = brandMapper.insert(brand) > 0;
            return b2;
        }
    }
    /*根据id查询一个brand*/

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean batchdelete(Long[] ids) {
        boolean b = false;
        for (Long id : ids) {
            b = brandMapper.deleteByPrimaryKey(id) > 0;
        }
        return b;
    }

    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}

