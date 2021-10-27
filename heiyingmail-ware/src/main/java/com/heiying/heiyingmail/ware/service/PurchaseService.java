package com.heiying.heiyingmail.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heiying.common.utils.PageUtils;
import com.heiying.heiyingmail.ware.entity.PurchaseEntity;
import com.heiying.heiyingmail.ware.vo.MergeVO;
import com.heiying.heiyingmail.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author heiying
 * @email 2749468247@qq.com
 * @date 2021-10-13 15:05:05
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergePurchase(MergeVO mergeVO);

    void received(List<Long> ids);

    void done(PurchaseDoneVo doneVo);
}

