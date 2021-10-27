package com.heiying.heiyingmail.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.heiying.heiyingmail.ware.vo.MergeVO;
import com.heiying.heiyingmail.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.heiying.heiyingmail.ware.entity.PurchaseEntity;
import com.heiying.heiyingmail.ware.service.PurchaseService;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.R;



/**
 * 采购信息
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 15:05:05
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;




    @PostMapping("/done")
    public R done(@Validated @RequestBody PurchaseDoneVo doneVo){
        purchaseService.done(doneVo);
        return R.ok();
    }


    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids){
        purchaseService.received(ids);
        return R.ok();
    }


    @PostMapping("/merge")
    public R merge(@RequestBody MergeVO mergeVO){
        purchaseService.mergePurchase(mergeVO);
        return R.ok();
    }


    @RequestMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceive(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setUpdateTime(new Date());
        purchase.setCreateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
