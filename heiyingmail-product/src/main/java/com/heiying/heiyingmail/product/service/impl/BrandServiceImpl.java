package com.heiying.heiyingmail.product.service.impl;

import com.heiying.heiyingmail.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.BrandDao;
import com.heiying.heiyingmail.product.entity.BrandEntity;
import com.heiying.heiyingmail.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //获取key
        String key= (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<BrandEntity>();
        if (StringUtils.isNotEmpty(key)){
            queryWrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }


    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        //保证冗余字段的数据一致
        updateById(brand);
        if (StringUtils.isNotEmpty(brand.getName())){
            //同步更新其他关联表中的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

            //TODO 更新其他关联
        }
    }

    @Override
    public List<BrandEntity> getBrandsById(List<Long> brandIds) {
        return baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id",brandIds));
    }

}