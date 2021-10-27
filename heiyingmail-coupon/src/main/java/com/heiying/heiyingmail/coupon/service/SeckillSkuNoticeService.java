package com.heiying.heiyingmail.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.coupon.entity.SeckillSkuNoticeEntity;

import java.util.Map;

/**
 * 秒杀商品通知订阅
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:15:24
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

