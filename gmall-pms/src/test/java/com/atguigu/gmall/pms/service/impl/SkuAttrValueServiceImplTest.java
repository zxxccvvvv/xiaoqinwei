package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SkuAttrValueServiceImplTest {

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Test
    void querySaleAttrValuesMappingSkuId() {
        System.out.println(skuAttrValueService.querySaleAttrValuesMappingSkuId(16l));
    }
}