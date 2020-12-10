package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping({"/", "/index"})
    public String toIndex(Model model){
        List<CategoryEntity> categoryEntities = indexService.queryLvl1Categories();
        model.addAttribute("categories", categoryEntities);
        //TODO: 加载其他数据
        return "index";
    }

    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSub(
            @PathVariable Long pid){

        List<CategoryEntity> categoryEntities = indexService.queryLvl2CategoriesWithSub(pid);
        return ResponseVo.ok(categoryEntities);
    }


    @GetMapping("index/test/lock")
    @ResponseBody
    public ResponseVo testLock(){
        indexService.testlock();
        return ResponseVo.ok();
    }

    @GetMapping("index/test/lock1")
    @ResponseBody
    public ResponseVo testLock1(){
        indexService.testlock1();
        return ResponseVo.ok();
    }

    @GetMapping("index/test/read")
    @ResponseBody
    public ResponseVo testRead(){
        indexService.testRead();
        return ResponseVo.ok();
    }

    @GetMapping("index/test/write")
    @ResponseBody
    public ResponseVo testWrite(){
        indexService.testWrite();
        return ResponseVo.ok();
    }
    @GetMapping("index/test/latch")
    @ResponseBody
    public ResponseVo  latch() throws InterruptedException {
        indexService.latch();
        return ResponseVo.ok();
    }

    @GetMapping("index/test/countdown")
    @ResponseBody
    public ResponseVo countdown(){
        indexService.countdown();
        return ResponseVo.ok();
    }
}
