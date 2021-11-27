package com.heiying.heiyingmail.product.service.impl;

import com.heiying.heiyingmail.product.vo.SkuItemVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.SkuSaleAttrValueDao;
import com.heiying.heiyingmail.product.entity.SkuSaleAttrValueEntity;
import com.heiying.heiyingmail.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemVO.SkuItemSaleAttrVO> getSaleAttrsBySpuId(Long spuId) {
        List<SkuItemVO.SkuItemSaleAttrVO> saleAttrVOS=baseMapper.getSaleAttrsBySpuId(spuId);
        return saleAttrVOS;
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        return baseMapper.getSkuSaleAttrValuesAsStringList(skuId);
    }

}