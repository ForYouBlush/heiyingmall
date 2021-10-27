package com.heiying.heiyingmail.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 14:43:33
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

