package com.heiying.heiyingmail.member.fegin;

import com.heiying.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("heiyingmail-coupon")
public interface MemberCouponsService {
    @RequestMapping("/coupon/coupon/coupons")
    public R coupons();
}
