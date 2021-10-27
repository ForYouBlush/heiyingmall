package com.heiying.heiyingmail.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:43:33
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

