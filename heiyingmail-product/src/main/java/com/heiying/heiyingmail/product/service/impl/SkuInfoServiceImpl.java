package com.heiying.heiyingmail.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.product.entity.SkuImagesEntity;
import com.heiying.heiyingmail.product.entity.SpuInfoDescEntity;
import com.heiying.heiyingmail.product.feign.SeckillFeignService;
import com.heiying.heiyingmail.product.service.*;
import com.heiying.heiyingmail.product.vo.SeckillInfoVO;
import com.heiying.heiyingmail.product.vo.SkuItemVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.SkuInfoDao;
import com.heiying.heiyingmail.product.entity.SkuInfoEntity;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    SkuImagesService imagesService;
    @Resource
    SpuInfoDescService descService;
    @Resource
    AttrGroupService attrGroupService;
    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    SeckillFeignService seckillFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equals(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (StringUtils.isNotEmpty(min)) {
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (StringUtils.isNotEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    wrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>()
                .eq("spu_id", spuId));
        return list;
    }

    @Override
    public SkuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1、sku基本信息获取
            SkuInfoEntity info = this.getById(skuId);
            skuItemVO.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3、获取spu的销售属性组合
            List<SkuItemVO.SkuItemSaleAttrVO> saleAttrVOS = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVO.setSaleAttr(saleAttrVOS);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4、获取spu的介绍
            SpuInfoDescEntity desc = descService.getById(res.getSpuId());
            skuItemVO.setDesc(desc);
        }, executor);


        CompletableFuture<Void> groupFuture = infoFuture.thenAcceptAsync((res) -> {
            //5、获取spu的规格参数信息
            List<SkuItemVO.SpuItemAttrGroupVO> attrGroupVOS = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVO.setGroupAttrs(attrGroupVOS);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2、sku的图片信息
            List<SkuImagesEntity> images = imagesService.getIamgesBySkuId(skuId);
            skuItemVO.setImages(images);
        }, executor);

        //3、查询当前商品是否参与秒杀优惠
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVO data = r.getData(new TypeReference<SeckillInfoVO>() {
                });
                skuItemVO.setSeckillInfo(data);
            }
        }, executor);


        //等待所有任务都完成
        CompletableFuture.allOf(saleAttrFuture, descFuture, groupFuture, imageFuture,seckillFuture).get();


        return skuItemVO;
    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfoEntity skuInfoEntity = baseMapper.selectOne(new QueryWrapper<SkuInfoEntity>().eq("sku_id", skuId));
        return skuInfoEntity.getPrice();
    }

}