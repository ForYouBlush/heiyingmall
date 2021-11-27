package com.heiying.heiyingmail.coupon.service.impl;

import com.heiying.heiyingmail.coupon.entity.SeckillSkuRelationEntity;
import com.heiying.heiyingmail.coupon.service.SeckillSkuRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heiying.common.utils.PageUtils;
import com.heiying.common.utils.Query;

import com.heiying.heiyingmail.coupon.dao.SeckillSessionDao;
import com.heiying.heiyingmail.coupon.entity.SeckillSessionEntity;
import com.heiying.heiyingmail.coupon.service.SeckillSessionService;


@Slf4j
@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {

        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime(), endTime()));

        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                List<SeckillSkuRelationEntity> entities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>()
                        .eq("promotion_session_id", session.getId()));
                session.setRelationSkus(entities);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime startTime = LocalDateTime.of(now, LocalTime.MIN);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(2);
        LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
        return endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}