package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:50
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {

    List<CategoryEntity> queryCategoriesByPid(Long pid);
}
