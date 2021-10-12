package com.heiying.heiyingmail.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-12 15:12:53
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

