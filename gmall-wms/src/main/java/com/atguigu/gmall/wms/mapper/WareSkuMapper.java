package com.atguigu.gmall.wms.mapper;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-29 12:28:40
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {


    int lock(@Param("skuId") Long id, @Param("count") Integer count);
    int unlock(@Param("skuId") Long id, @Param("count") Integer count);


    List<WareSkuEntity> checkLock(@Param("skuId") Long skuId, @Param("count") Integer count);

}
