package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:50
 */
@Mapper
public interface CommentReplayMapper extends BaseMapper<CommentReplayEntity> {
	
}
