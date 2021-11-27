package com.heiying.heiyingmail.product.web;

import com.heiying.heiyingmail.product.service.SkuInfoService;
import com.heiying.heiyingmail.product.vo.SkuItemVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {
    @Resource
    SkuInfoService skuInfoService;
    /**
     * 展示当前sku的详情
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVO itemVO=skuInfoService.item(skuId);
        model.addAttribute("item",itemVO);
        return "item";
    }
}
