package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 16:41:39
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemEntity> {
	
}
