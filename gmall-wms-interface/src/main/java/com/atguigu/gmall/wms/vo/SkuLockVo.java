package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @description:
 * @author: XQW
 * @date: 2020/11/22 15:47
 */
@Data
public class SkuLockVo {

    private Long skuId;
    private Integer count;
    private Boolean lock;

    //锁定库存成功的Id
    private Long wareSkuId;

}
