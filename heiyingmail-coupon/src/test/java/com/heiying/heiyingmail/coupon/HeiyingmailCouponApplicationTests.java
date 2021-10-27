package com.heiying.heiyingmail.coupon;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.heiying.heiyingmail.coupon.controller.CouponController;
import com.heiying.heiyingmail.coupon.entity.CouponEntity;
import com.heiying.heiyingmail.coupon.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
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

}
