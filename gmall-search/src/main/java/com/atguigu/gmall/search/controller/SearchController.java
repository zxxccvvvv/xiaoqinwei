package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping
    public String search(SearchParamVo paramVo, Model model){

        SearchResponseVo responseVo = searchService.search(paramVo);
        System.out.println("response===========" + responseVo );

        model.addAttribute("searchParam", paramVo);
        model.addAttribute("response", responseVo);

        return "search";
    }


}
