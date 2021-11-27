package com.heiying.heiyingmail.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.constant.ProductConstant;
import com.heiying.common.to.SkuReductionTo;
import com.heiying.common.to.SpuBoundTo;
import com.heiying.common.to.es.SkuEsModel;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.product.dao.SpuInfoDao;
import com.heiying.heiyingmail.product.entity.*;
import com.heiying.heiyingmail.product.feign.CouponFeignService;
import com.heiying.heiyingmail.product.feign.SearchFeignService;
import com.heiying.heiyingmail.product.feign.WareFeignService;
import com.heiying.heiyingmail.product.service.*;
import com.heiying.heiyingmail.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    SpuInfoDescService descService;
    @Resource
    SpuImagesService imagesService;
    @Resource
    AttrService attrService;
    @Resource
    ProductAttrValueService valueService;
    @Resource
    SkuInfoService skuInfoService;
    @Resource
    SkuImagesService skuImagesService;
    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    CouponFeignService couponFeignService;
    @Resource
    BrandService brandService;
    @Resource
    CategoryService categoryService;
    @Resource
    WareFeignService wareFeignService;
    @Resource
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     *
     *
     * @param vo
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVO vo) {
        //1、保存spu基本信息   pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveSpuInfo(spuInfoEntity);


        //2、保存spu的描述图片  pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        descService.saveSpuInfoDesc(descEntity);


        //3、保存spu的图片集    pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(spuInfoEntity.getId(), images);


        //4、保存spu的规格参数    pms_sku_sale_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(item.getAttrId());
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());
            valueEntity.setAttrValue(item.getAttrValues());
            valueEntity.setQuickShow(item.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        valueService.saveProductAttr(collect);

        //5、保存spu的积分信息      跨库heiyingmail_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }
        //5、保存当前spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息     pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntityList = item.getImages().stream().map(img -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    imagesEntity.setSkuId(skuId);
                    imagesEntity.setImgUrl(img.getImgUrl());
                    imagesEntity.setDefaultImg(img.getDefaultImg());
                    return imagesEntity;
                }).filter(entity -> {
                    //过滤空的图片
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息     pms_sku_images
                skuImagesService.saveBatch(imagesEntityList);

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity saleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, saleAttrValueEntity);
                    saleAttrValueEntity.setSkuId(skuId);
                    return saleAttrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息  pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(saleAttrValueEntities);


                //5.4）、sku的优惠、满减、会员价等信息
                // 跨库heiyingmail_sms->sms_sku_ladder、sms_sku_full_reduction、sms_member_price
                SkuReductionTo reductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, reductionTo);
                reductionTo.setSkuId(skuId);
                if (reductionTo.getFullCount() > 0 || reductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(reductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }


    }

    @Override
    public void saveSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equals(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catlogId = (String) params.get("catalogId");
        if (StringUtils.isNotEmpty(catlogId) && !"0".equals(catlogId)) {
            queryWrapper.eq("catalog_id", catlogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品上架
     *
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        //查出当前spuid对应的sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIDs = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 查询当前sku所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> valueEntities = valueService.baseAttrListforspu(spuId);
        List<Long> attrIds = valueEntities.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = valueEntities.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());


        // 发送远程调用，库存系统查询是否还有库存
        Map<Long, Boolean> map = null;
        try {
            R hasStock = wareFeignService.getSkusHasStock(skuIDs);
            TypeReference<List<SkuHasStockVO>> typeReference = new TypeReference<List<SkuHasStockVO>>() {
            };
            map = hasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVO::getSkuId, item -> item.getHasStock()));

        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }

        //封装每个sku的信息
        Map<Long, Boolean> finalMap = map;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            //组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuImg(sku.getSkuDefaultImg());
            esModel.setSkuPrice(sku.getPrice());

            //设置库存信息
            if (finalMap == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalMap.get(sku.getSkuId()));
                esModel.setHasStock(finalMap.get(sku.getSkuId()));
            }
            //热度评分。 0
            esModel.setHotScore(0L);


            //查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());

        // 将数据发给es进行保存；heiyingmail-search
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            //远程调用成功
            // 修改当前spu的状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            //TODO 重复调用？接口幂等性，重试机制？
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        SpuInfoEntity spuInfoEntity = getById(byId.getSpuId());
        return spuInfoEntity;
    }


}