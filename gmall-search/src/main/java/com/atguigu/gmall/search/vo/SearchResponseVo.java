package com.atguigu.gmall.search.vo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {
    //品牌过滤集合
    private List<BrandEntity> brands;

    //分类过滤集合
    private List<CategoryEntity> categories;
    //规格参数的过滤，每个元素是一行过滤条件
    private List<SearchResponseAttrVo> filters;

    //分页响应结果
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Long total;

    //当前页的记录
    private List<Goods> goodsList;
}
