package com.atguigu.gmall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {

    private String keyword; //产品条件

    private List<Long> brandId; //品牌过滤

    private List<Long> categoryId;  //分类过滤

    private List<String> props;  //过滤的检索参数

    private Integer sort = 0;  //排序字段

    //价格区间
    private Double priceFrom;
    private Double priceTo;

    //页码
    private Integer pageNum = 1;
    private final Integer pageSize = 20;

    //是否有货
    private Boolean store;




}
