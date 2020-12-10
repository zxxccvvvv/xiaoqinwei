package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    //根据spuid查询spu信息
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);


    //分页查询已上架的SPU信息
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    //根据SpuId查询对应的SKU信息（接口已写好）querySkusBySpuId
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySpuId(
            @PathVariable("spuId") Long spuId);



    //根据skuid查询sku信息。
    @GetMapping("pms/sku/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    //根据skuId查询sku图片信息
    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(
            @PathVariable("skuId") Long skuId);

    //根据skuId查询skuAttr属性
    @GetMapping("pms/skuattrvalue/sale/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrsBySkuId(
            @PathVariable("skuId")Long skuId);

    //根据spuId查询skuAttr属性值
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId")Long spuId);

    //根据spuId查询skuAttr属性与skuId的映射关系
    @GetMapping("pms/skuattrvalue/sku/mapping/{spuId}")
    public ResponseVo<String> querySaleAttrValuesMappingSkuId(@PathVariable("spuId")Long spuId);

    //根据cid spuId skuId 查询分组信息
    @GetMapping("pms/attrgroup/cid/spuId/skuId/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId);

    //根据分类id查询商品分类（逆向工程已自动生成）
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //根据分类pid查询商品分类列表
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategory(
            @PathVariable("parentId") Long pid);

    //根据pid查询二级三级分类
    @GetMapping("pms/category/subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesWithSub(
            @PathVariable("pid") Long pid);

    //根据3级分类id查询一二三级分类信息。
    @GetMapping("pms/category/all/{cid}")
    public ResponseVo<List<CategoryEntity>> queryAllLvlCategoriesByCid3(
            @PathVariable Long cid);

    // 根据品牌id查询品牌（逆向工程已自动生成）
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);



    // 根据spuId查询检索规格参数及值
    @GetMapping("pms/spuattrvalue/search/attr/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchSpuAttrValueBySpuIdAndCid(
            @PathVariable("spuId") Long spuId,
            @RequestParam("cid") Long cid);

    // 根据skuId查询检索规格参数及值
    @GetMapping("pms/skuattrvalue/search/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuIdAndCid(
            @PathVariable(value = "skuId") Long skuId,
            @RequestParam("cid") Long cid);

    //根据spuId查询spu描述信息
    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

}
