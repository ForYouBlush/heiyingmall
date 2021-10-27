package com.heiying.heiyingmail.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:53
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

