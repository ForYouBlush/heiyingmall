package com.heiying.heiyingmail.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.heiying.common.valid.AddGroup;
import com.heiying.common.valid.UpdateGroup;
import com.heiying.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.heiying.heiyingmail.product.entity.BrandEntity;
import com.heiying.heiyingmail.product.service.BrandService;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.R;


/**
 * 品牌
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:53
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandsById(brandIds);

        return R.ok().put("brand", brand);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
//        Map<String, String> map = new HashMap<>();
//        if (result.hasErrors()) {
//            result.getFieldErrors().forEach((item) -> {
//                String field = item.getField();
//                String message = item.getDefaultMessage();
//                map.put(field, message);
//            });
//            return R.error(400, "提交的数据不合法").put("data", map);
//        } else {
//        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     *  修改状态
     * @param brand
     * @return
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated({UpdateStatusGroup.class}) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
