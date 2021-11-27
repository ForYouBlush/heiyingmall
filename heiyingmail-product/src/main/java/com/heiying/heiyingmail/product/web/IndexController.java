package com.heiying.heiyingmail.product.web;

import com.heiying.heiyingmail.product.entity.CategoryEntity;
import com.heiying.heiyingmail.product.service.CategoryService;
import com.heiying.heiyingmail.product.vo.Catalog2VO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Resource
    CategoryService categoryService;


    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categorys=categoryService.getLevel1Categorys();
        model.addAttribute("categorys",categorys);
        return "index";
    }
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catalog2VO>> getCatalogJson(){
        Map<String,List<Catalog2VO>> map=categoryService.getCatalogJson();
        return map;
    }
}
