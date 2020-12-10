package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 属性分组
 *
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:50
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryByCid(Long catId);

    List<GroupVo> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId);
}

