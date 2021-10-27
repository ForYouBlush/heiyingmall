package com.heiying.heiyingmail.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heiying.heiyingmail.product.entity.BrandEntity;
import com.heiying.heiyingmail.product.vo.BrandVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.heiying.heiyingmail.product.entity.CategoryBrandRelationEntity;
import com.heiying.heiyingmail.product.service.CategoryBrandRelationService;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:54
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


    /**
     * 获取当前品牌关联的所有分类列表
     */
    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId)
        );
        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }


    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam(value = "catId", required = true) Long catId) {
        List<BrandVO> vos = categoryBrandRelationService.getBrandsByCatId(catId);

        return R.ok().put("data", vos);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
