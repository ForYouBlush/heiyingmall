package com.heiying.heiyingmail.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.ware.entity.WareInfoEntity;
import com.heiying.heiyingmail.ware.vo.FareVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 15:05:05
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVO getFare(Long addrId);
}

