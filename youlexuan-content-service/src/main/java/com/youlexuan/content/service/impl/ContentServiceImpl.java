package com.youlexuan.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
//import com.youlexuan.CONSTANT;
import com.youlexuan.content.service.ContentService;
import com.ujiuye.entity.PageResult;
import com.ujiuye.mapper.TbContentMapper;
import com.ujiuye.pojo.TbContent;
import com.ujiuye.pojo.TbContentExample;
import com.ujiuye.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     * redis和mysql的数据同步
     */
    @Override
    public void add(TbContent content) {
//		redisTemplate.boundHashOps(CONSTANT.CONTENT_lIST_KEY).delete(content.getCategoryId());
        contentMapper.insert(content);
        //当广告信息发生改变就要清空缓存保证数据与数据库同步
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }


    /**
     * 修改
     * content 的类型由1改成了4
     * 判断原来的categoryId 和新修改的categoryId是否一致
     * 一致：从缓存中删除
     * 不一致：把老的redis删了1，再把新的redis删了4。
     */
    @Override
    public void update(TbContent content) {
        //查询修改前的分类Id
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        redisTemplate.boundHashOps("content").delete(categoryId);

//        Long oldCategoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
//        Long newCategoryId = content.getCategoryId();
//		redisTemplate.boundHashOps(CONSTANT.CONTENT_lIST_KEY).delete(newCategoryId);

//        if (!oldCategoryId.equals(newCategoryId)) {
//			redisTemplate.boundHashOps(CONSTANT.CONTENT_lIST_KEY).delete(oldCategoryId);
//        }
        contentMapper.updateByPrimaryKey(content);
        //如果分类ID发生了修改,清除修改后的分类ID的缓存
        if (categoryId.longValue() != content.getCategoryId().longValue()) {
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //清除缓存
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();//广告分类ID
            redisTemplate.boundHashOps("content").delete(categoryId);
//            TbContent content = contentMapper.selectByPrimaryKey(id);
//			redisTemplate.boundHashOps(CONSTANT.CONTENT_lIST_KEY).delete(content.getCategoryId());
            contentMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }
        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 先从redis中查询列表
     * 如果redis中存在，那么返回
     * 如果redis中不存在，数据库中查询，并存放到redis中一份
     * <p>
     * redis中存放数据格式：hash类型 redis大KEY固定值 contentList。 fileId categoryId fildVlaue  List<TbContent>
     *
     * @param categoryId
     * @return
     */
//	@Override
//	public List<TbContent> findByCategoryId(Long categoryId) {
//
//		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps(CONSTANT.CONTENT_lIST_KEY).get(categoryId);
//		if(contentList==null){
//			TbContentExample exam = new TbContentExample();
//			exam.setOrderByClause("sort_order");
//			Criteria criteria = exam.createCriteria();
//			criteria.andCategoryIdEqualTo(categoryId);
//			criteria.andStatusEqualTo("1");
//			contentList =  contentMapper.selectByExample(exam);
//			redisTemplate.boundHashOps(CONSTANT.CONTENT_lIST_KEY).put(categoryId,contentList);
//			System.out.println("db find ....");
//		} else {
//			System.out.println("redis find...");
//		}
//
//		return contentList;
//
//
//	}
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        /*从缓存中查询是否有根据分类查询的广告数据*/
        List<TbContent> contentList = (List<TbContent>)
                redisTemplate.boundHashOps("content").get(categoryId);
        if (contentList == null) {
            //根据广告分类ID查询广告列表
            TbContentExample contentExample = new TbContentExample();
            Criteria criteria2 = contentExample.createCriteria();
            criteria2.andCategoryIdEqualTo(categoryId);
            criteria2.andStatusEqualTo("1");//开启状态
            contentExample.setOrderByClause("sort_order");//排序
            contentList = contentMapper.selectByExample(contentExample);
            System.out.println("从数据库中读取数据");
            //查完后放入到redis中
            redisTemplate.boundHashOps("content").put(categoryId, contentList);
        } else {
            System.out.println("从缓存中读取数据");
        }
        return contentList;
    }
}
