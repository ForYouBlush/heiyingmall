package com.heiying.heiyingmail.coupon.service.impl;

import com.heiying.common.to.MemberPrice;
import com.heiying.common.to.SkuReductionTo;
import com.heiying.heiyingmail.coupon.entity.MemberPriceEntity;
import com.heiying.heiyingmail.coupon.entity.SkuLadderEntity;
import com.heiying.heiyingmail.coupon.service.MemberPriceService;
import com.heiying.heiyingmail.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.coupon.dao.SkuFullReductionDao;
import com.heiying.heiyingmail.coupon.entity.SkuFullReductionEntity;
import com.heiying.heiyingmail.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    SkuLadderService skuLadderService;
    @Resource
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1、保存满减打折、会员价      sku的优惠、满减、会员价等信息
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(skuReductionTo.getSkuId());
        ladderEntity.setFullCount(skuReductionTo.getFullCount());
        ladderEntity.setDiscount(skuReductionTo.getDiscount());
        ladderEntity.setAddOther(skuReductionTo.getCountStatus());
        //sku的优惠
        if (skuReductionTo.getFullCount()>0){
            skuLadderService.save(ladderEntity);
        }

        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        //满减
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0"))==1){
            this.save(skuFullReductionEntity);
        }


        //会员价
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item->{
            return item.getMemberPrice().compareTo(new BigDecimal("0"))==1;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}