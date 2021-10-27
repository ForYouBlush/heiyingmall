package com.heiying.heiyingmail.ware.service.impl;

import com.heiying.common.constant.WareConstant;
import com.heiying.heiyingmail.ware.entity.PurchaseDetailEntity;
import com.heiying.heiyingmail.ware.service.PurchaseDetailService;
import com.heiying.heiyingmail.ware.service.WareSkuService;
import com.heiying.heiyingmail.ware.vo.MergeVO;
import com.heiying.heiyingmail.ware.vo.PurchaseDoneVo;
import com.heiying.heiyingmail.ware.vo.PurchaseItemDoneVO;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.ware.dao.PurchaseDao;
import com.heiying.heiyingmail.ware.entity.PurchaseEntity;
import com.heiying.heiyingmail.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    PurchaseDetailService detailService;
    @Resource
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper.eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void mergePurchase(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if (purchaseId == null) {
            //新建一个
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }
        //TODO 确认采购单的状态是0，1才可以合并
        List<Long> items = mergeVO.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());


        detailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     * @param ids 采购单的id
     */
    @Override
    @Transactional
    public void received(List<Long> ids) {
        //1、确认当前采购单是新建或已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            } else {
                return false;
            }
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        //2、改变采购单的状态
        this.updateBatchById(collect);
        //3、改变采购项的状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> detailEntityList = detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> entities = detailEntityList.stream().map(en -> {
                PurchaseDetailEntity entity = new PurchaseDetailEntity();
                entity.setId(en.getId());
                entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity;
            }).collect(Collectors.toList());
            detailService.updateBatchById(entities);
        });
    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();
        //1、改变采购项的状态
        Boolean flag = true;
        List<PurchaseItemDoneVO> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVO item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
            } else {
                //3、将成功采购的进行入库
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);

        }
        detailService.updateBatchById(updates);

        //2、改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode()
                : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}