package com.heiying.heiyingmail.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 15:05:05
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

