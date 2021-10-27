package com.heiying.heiyingmail.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.ware.dao.PurchaseDetailDao;
import com.heiying.heiyingmail.ware.entity.PurchaseDetailEntity;
import com.heiying.heiyingmail.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String key= (String) params.get("key");
        if (StringUtils.isNotEmpty(key)){
            wrapper.and(w->{
                w.eq("purchase_id",key).or().eq("sku_id",key);
            });
        }
        String status= (String) params.get("status");
        if (StringUtils.isNotEmpty(status)){
            wrapper.eq("status",status);
        }
        String wareId= (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<PurchaseDetailEntity> purchase_id = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        return purchase_id;
    }

}