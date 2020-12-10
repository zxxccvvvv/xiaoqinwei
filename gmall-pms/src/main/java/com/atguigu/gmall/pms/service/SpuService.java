package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:49
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuInfo(Long categoryId, PageParamVo pageParamVo);

    void bigSave(SpuVo spuVo);
}

