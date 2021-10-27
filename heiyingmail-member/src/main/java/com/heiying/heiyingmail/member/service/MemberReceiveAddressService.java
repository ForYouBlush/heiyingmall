package com.heiying.heiyingmail.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.member.entity.MemberReceiveAddressEntity;

import java.util.Map;

/**
 * 会员收货地址
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:43:34
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

