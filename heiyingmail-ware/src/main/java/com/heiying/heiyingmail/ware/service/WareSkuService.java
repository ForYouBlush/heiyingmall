package com.heiying.heiyingmail.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.to.mq.OrderTO;
import com.heiying.common.to.mq.StockLockedTO;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.ware.entity.WareSkuEntity;
import com.heiying.heiyingmail.ware.vo.LockStockResult;
import com.heiying.heiyingmail.ware.vo.SkuHasStockVO;
import com.heiying.heiyingmail.ware.vo.WareSkuLockVO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 15:05:05
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVO> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVO lockVO);

    void unlockStock(StockLockedTO to);

    void unlockStock(OrderTO to);
}

