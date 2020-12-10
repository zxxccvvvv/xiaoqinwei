package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品三级分类
 *
 * @author xiaobai
 * @email xiaobai@atguigu.com
 * @date 2020-10-28 15:19:50
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("all/{cid}")
    public ResponseVo<List<CategoryEntity>> queryAllLvlCategoriesByCid3(@PathVariable Long cid){
        List<CategoryEntity> categoryEntities = categoryService.queryAllLvlCategoriesByCid3(cid);
        return ResponseVo.ok(categoryEntities);
    }

    //根据pid查分类信息
    @GetMapping("subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesWithSub(
            @PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntities =  categoryService.queryCategoriesWithSub(pid);
        return ResponseVo.ok(categoryEntities);

    }

    /**
     * 根据父id查询方法

     */
    @GetMapping("parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategory(
            @PathVariable("parentId") Long pid){
        List<CategoryEntity> categoryEntityList = categoryService.queryCategory(pid);
        return ResponseVo.ok(categoryEntityList);

    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCategoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = categoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id){
		CategoryEntity category = categoryService.getById(id);

        return ResponseVo.ok(category);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		categoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
