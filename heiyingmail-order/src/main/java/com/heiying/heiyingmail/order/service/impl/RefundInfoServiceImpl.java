package com.heiying.heiyingmail.order.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.order.dao.RefundInfoDao;
import com.heiying.heiyingmail.order.entity.RefundInfoEntity;
import com.heiying.heiyingmail.order.service.RefundInfoService;


@Service("refundInfoService")
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoDao, RefundInfoEntity> implements RefundInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<RefundInfoEntity> page = this.page(
                new Query<RefundInfoEntity>().getPage(params),
                new QueryWrapper<RefundInfoEntity>()
        );

        return new PageUtils(page);
    }

}