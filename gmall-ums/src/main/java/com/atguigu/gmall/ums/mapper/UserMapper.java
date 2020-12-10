package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 17:26:25
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
