package com.heiying.heiyingmail.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.heiying.common.exception.BizCodeEnume;
import com.heiying.heiyingmail.ware.vo.LockStockResult;
import com.heiying.heiyingmail.ware.vo.SkuHasStockVO;
import com.heiying.heiyingmail.ware.vo.WareSkuLockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.heiying.heiyingmail.ware.entity.WareSkuEntity;
import com.heiying.heiyingmail.ware.service.WareSkuService;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.R;



/**
 * 商品库存
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 15:05:05
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVO lockVO){
        Boolean stock= null;
        try {
            stock = wareSkuService.orderLockStock(lockVO);
            return R.ok();
        } catch (Exception e) {
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(),BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }

    }

    //查询sku是否有库存
    @PostMapping("/hasstock")
    public  R getSkusHasStock(@RequestBody List<Long> skuIds){
            List<SkuHasStockVO> vos=wareSkuService.getSkusHasStock(skuIds);
            return R.ok().setData(vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
