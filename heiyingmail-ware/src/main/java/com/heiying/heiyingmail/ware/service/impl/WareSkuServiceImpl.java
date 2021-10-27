package com.heiying.heiyingmail.ware.service.impl;

import com.heiying.common.utils.R;
import com.heiying.heiyingmail.ware.feign.ProductFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.ware.dao.WareSkuDao;
import com.heiying.heiyingmail.ware.entity.WareSkuEntity;
import com.heiying.heiyingmail.ware.service.WareSkuService;

import javax.annotation.Resource;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId= (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }
        String wareId= (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_id",skuId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、如果还没有这个仓库记录就新增
        List<WareSkuEntity> skuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (skuEntities==null||skuEntities.size()==0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO 自己捕捉异常，发生异常时不回滚。还有什么方法吗？？？
            try {
                R info = productFeignService.info(skuId);
                Map<String ,Object> map= (Map<String, Object>) info.get("skuInfo");
                if (info.getCode()==0){
                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }

            }catch (Exception e){

            }
            this.baseMapper.insert(wareSkuEntity);
        }else {
            this.baseMapper.addStock(skuId,wareId,skuNum);
        }
    }

}