package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户表
 *
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 17:26:25
 */
public interface UserService extends IService<UserEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    Boolean checkData(String data, Integer type);

    void register(UserEntity userEntity, String code);

    UserEntity queryUser(String loginName, String password);
}

