package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {
    //查询用户是否存在
    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUser(
            @RequestParam("loginName")String loginName,
            @RequestParam("password")String password);

    @GetMapping("ums/useraddress/user/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryAddressesByUserId(@PathVariable("userId") Long userId);
    //根据用户id查询用户信息
    @GetMapping("ums/user/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);

}
