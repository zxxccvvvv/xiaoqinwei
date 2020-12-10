package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategory(Long pid) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        //如果parentId为-1,说明用户没有传该字段, 查询所有
        if (pid != -1){
            wrapper.eq("parent_id", pid);
        }

        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSub(Long pid) {
        return categoryMapper.queryCategoriesByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryAllLvlCategoriesByCid3(Long cid) {
        //1.根据三级分类id查询三级分类
        CategoryEntity categoryEntity3 = this.getById(cid);
        if (categoryEntity3 == null){
            return null;
        }

        //2.根据三级分类id查询二级分类
        CategoryEntity categoryEntity2 = this.getById(categoryEntity3.getParentId());
        if (categoryEntity2 == null){
            return null;
        }
        //3.根据二级分类id查询1级分类
        CategoryEntity categoryEntity1 = this.getById(categoryEntity2.getParentId());
        return Arrays.asList(categoryEntity1, categoryEntity2, categoryEntity3);
    }

}