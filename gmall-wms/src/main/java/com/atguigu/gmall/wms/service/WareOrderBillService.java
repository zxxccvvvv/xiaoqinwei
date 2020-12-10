package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.wms.entity.WareOrderBillEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-29 12:28:40
 */
public interface WareOrderBillService extends IService<WareOrderBillEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

