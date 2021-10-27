package com.heiying.heiyingmail.coupon.dao;

import com.heiying.heiyingmail.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:15:23
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
