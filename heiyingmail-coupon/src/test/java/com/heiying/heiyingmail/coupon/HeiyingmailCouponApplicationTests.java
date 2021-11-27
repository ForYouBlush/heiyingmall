package com.heiying.heiyingmail.coupon;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.heiying.heiyingmail.coupon.controller.CouponController;
import com.heiying.heiyingmail.coupon.entity.CouponEntity;
import com.heiying.heiyingmail.coupon.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

//@SpringBootTest
class HeiyingmailCouponApplicationTests {
    @Resource
    CouponService couponService;
    @Test
    void contextLoads() {
        CouponEntity entity=new CouponEntity();
        entity.setCode("200");
        couponService.save(entity);
        System.out.println("插入成功");
    }
    @Test
    public void page(){
        QueryChainWrapper<CouponEntity> wrapper = couponService.query();
        System.out.println( wrapper.list());
    }
    @Test
    public void test(){
        LocalDate now = LocalDate.now();
        LocalDateTime startTime = LocalDateTime.of(now, LocalTime.MIN);
//        long l = Long.parseLong(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        System.out.println(l);
        LocalDateTime of = LocalDateTime.of(2021, 11, 26, 10, 0, 0);
        Date date = new Date(2021, 11, 26, 10, 0, 0);
        long time = date.getTime();
        System.out.println(time);
        System.out.println(new Date().getTime());

    }

}
