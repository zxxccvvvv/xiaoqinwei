package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:50
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    //根据spuId查询skuAttr属性与skuId的映射关系
    @GetMapping("sku/mapping/{spuId}")
    public ResponseVo<String> querySaleAttrValuesMappingSkuId(@PathVariable("spuId")Long spuId){
        String jsonString = skuAttrValueService.querySaleAttrValuesMappingSkuId(spuId);
        return ResponseVo.ok(jsonString);

    }

    //根据spuId查询skuAttr属性值
    @GetMapping("spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId")Long spuId){

        List<SaleAttrValueVo> saleAttrValueVos =  skuAttrValueService.querySaleAttrValuesBySpuId(spuId);
        return ResponseVo.ok(saleAttrValueVos);

    }

    //根据skuId查询skuAttr属性
    @GetMapping("sale/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrsBySkuId(@PathVariable("skuId")Long skuId){
        List<SkuAttrValueEntity> skuAttrValueEntities =  skuAttrValueService.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
        return ResponseVo.ok(skuAttrValueEntities);
    }

    //根据分类id和skuId查询属性值
    @GetMapping("search/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuAttrValueBySkuIdAndCid(
            @PathVariable(value = "skuId") Long skuId,
            @RequestParam("cid") Long cid) {
        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueService.querySearchAttrValueBySkuIdAndCid(skuId, cid);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id){
		SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
