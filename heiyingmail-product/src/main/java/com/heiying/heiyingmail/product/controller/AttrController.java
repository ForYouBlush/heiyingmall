package com.heiying.heiyingmail.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.heiying.heiyingmail.product.entity.ProductAttrValueEntity;
import com.heiying.heiyingmail.product.service.ProductAttrValueService;
import com.heiying.heiyingmail.product.vo.AttrRespVO;
import com.heiying.heiyingmail.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.heiying.heiyingmail.product.entity.AttrEntity;
import com.heiying.heiyingmail.product.service.AttrService;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.R;


/**
 * 商品属性
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:54
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListforspu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities=productAttrValueService.baseAttrListforspu(spuId);
        return R.ok().put("data",entities);
    }



    /**
     * 列表
     */
    @GetMapping("/{attrType}/list/{catlogId}")
    public R baseList(@RequestParam Map<String, Object> params,
                      @PathVariable("attrType") String type,
                      @PathVariable("catlogId") Long catlogId) {
        PageUtils page = attrService.queryPage(params,catlogId,type);

        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }



    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrRespVO attrRespVO = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrRespVO);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVO attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }




    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVO attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }


    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities) {
        productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
