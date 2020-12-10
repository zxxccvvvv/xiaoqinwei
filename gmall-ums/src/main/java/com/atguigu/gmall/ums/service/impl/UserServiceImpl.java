package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        switch (type){
            case 1: queryWrapper.eq("username", data); break;
            case 2: queryWrapper.eq("phone", data); break;
            case 3: queryWrapper.eq("email", data); break;
            default:
                return null;
        }
        return userMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        //TODO:获取验证码
        //生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);
        //对密码加密
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword() + salt));
        //设置其他属性
        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1L);
        userEntity.setStatus(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);

        userEntity.setNickname(userEntity.getUsername());
        //添加到数据库
        boolean b = save(userEntity);

        //TODO:注册成功删除redis中的验证码



    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        //根据用户的登录信息查询用户列表
        List<UserEntity> userEntities = list(new QueryWrapper<UserEntity>().or(wrapper -> wrapper
                .eq("username", loginName).or()
                .eq("email", loginName).or()
                .eq("phone", loginName)));
        //判断登录输入是否合法
        if (CollectionUtils.isEmpty(userEntities)){
            return null;
            //threw new RuntimeException("用户名输入不合法")
        }
        for (UserEntity userEntity : userEntities) {
            //获取每个用户的盐，对用户的盐和密码进行加密
            String salt = userEntity.getSalt();
            String pwd = DigestUtils.md5Hex(password + salt);
            //比较用户输入的密码和当前用户在数据库中的密码进行比较
            if (StringUtils.equals(userEntity.getPassword(), pwd)){
                return userEntity;
            }
        }
        return null;
    }

}