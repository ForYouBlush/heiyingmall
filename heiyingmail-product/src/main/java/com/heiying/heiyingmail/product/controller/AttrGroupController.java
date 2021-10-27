package com.heiying.heiyingmail.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.heiying.heiyingmail.product.entity.AttrEntity;
import com.heiying.heiyingmail.product.service.AttrAttrgroupRelationService;
import com.heiying.heiyingmail.product.service.AttrService;
import com.heiying.heiyingmail.product.service.CategoryService;
import com.heiying.heiyingmail.product.vo.AttrGroupRelationVO;
import com.heiying.heiyingmail.product.vo.AttrGroupWithAttrsVO;
import com.heiying.heiyingmail.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.heiying.heiyingmail.product.entity.AttrGroupEntity;
import com.heiying.heiyingmail.product.service.AttrGroupService;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.R;


/**
 * 属性分组
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:54
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService relationService;
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVO> vos){
            relationService.saveBatch(vos);
            return R.ok();
    }


    @GetMapping("{catlogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catlogId") Long catlogId){
            List<AttrGroupWithAttrsVO> list=attrGroupService.getAttrGroupWithAttrsByCatlogId(catlogId);
            return R.ok().put("data",list);
    }



    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> list=attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",list);
    }



    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVO[] vos){
        attrService.deleteRelation(vos);
        return R.ok();
    }


    ///product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils page=attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }



    /**
     * 列表
     */
    @RequestMapping("/list/{catlogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catlogId") Long catlogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catlogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path=categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attr) {
        attrGroupService.save(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
