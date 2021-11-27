package com.heiying.heiyingmail.product.vo;

import com.heiying.heiyingmail.product.entity.SkuImagesEntity;
import com.heiying.heiyingmail.product.entity.SkuInfoEntity;
import com.heiying.heiyingmail.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemVO {
    //1、sku基本信息获取
    SkuInfoEntity info;
    Boolean hasStock=true;
    //2、sku的图片信息
    List<SkuImagesEntity> images;
    //3、获取spu的销售属性组合
    List<SkuItemSaleAttrVO> saleAttr;
    //4、获取spu的介绍
    SpuInfoDescEntity desc;
    //5、获取spu的规格参数信息
    List<SpuItemAttrGroupVO> groupAttrs;
    //6、当前商品的秒杀优惠信息
    SeckillInfoVO seckillInfo;


    @Data
    public static class SkuItemSaleAttrVO{
        private long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    @Data
    @ToString
    public static class SpuItemAttrGroupVO{
        private String groupName;
        private List<SpuBaseAttrVO> attrs;
    }

    @Data
    @ToString
    public static class SpuBaseAttrVO{
        private String attrName;
        private String attrValue;
    }
}
