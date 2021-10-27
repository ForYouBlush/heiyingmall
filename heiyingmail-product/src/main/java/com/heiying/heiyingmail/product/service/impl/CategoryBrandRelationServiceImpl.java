package com.heiying.heiyingmail.product.service.impl;

import com.heiying.heiyingmail.product.dao.BrandDao;
import com.heiying.heiyingmail.product.dao.CategoryDao;
import com.heiying.heiyingmail.product.entity.BrandEntity;
import com.heiying.heiyingmail.product.entity.CategoryEntity;
import com.heiying.heiyingmail.product.service.BrandService;
import com.heiying.heiyingmail.product.vo.BrandVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.product.dao.CategoryBrandRelationDao;
import com.heiying.heiyingmail.product.entity.CategoryBrandRelationEntity;
import com.heiying.heiyingmail.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    BrandDao brandDao;
    @Resource
    CategoryDao categoryDao;
    @Resource
    CategoryBrandRelationDao relationDao;
    @Resource
    BrandService brandService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        //查询详细名字
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId);
        relationEntity.setBrandName(name);
        this.update(relationEntity,new QueryWrapper<CategoryBrandRelationEntity>()
                .eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<BrandVO> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>()
                .eq("catelog_id", catId));
        List<BrandEntity> collect = relationEntities.stream().map(item -> {
            BrandEntity byId = brandService.getById(item.getBrandId());
            return byId;
        }).collect(Collectors.toList());
        List<BrandVO> collect2 = collect.stream().filter(e->{
            return e!=null;
        }).map(item -> {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandName(item.getName());
            brandVO.setBrandId(item.getBrandId());
            return brandVO;
        }).collect(Collectors.toList());
        return collect2;
    }

}