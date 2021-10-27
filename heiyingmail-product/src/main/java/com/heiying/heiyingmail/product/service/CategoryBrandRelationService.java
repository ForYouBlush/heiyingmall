package com.heiying.heiyingmail.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.product.entity.BrandEntity;
import com.heiying.heiyingmail.product.entity.CategoryBrandRelationEntity;
import com.heiying.heiyingmail.product.vo.BrandVO;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:54
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);

    List<BrandVO> getBrandsByCatId(Long catId);
}

