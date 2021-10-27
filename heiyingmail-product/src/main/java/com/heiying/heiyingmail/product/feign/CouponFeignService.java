package com.heiying.heiyingmail.product.feign;

import com.heiying.common.to.SkuReductionTo;
import com.heiying.common.to.SpuBoundTo;
import com.heiying.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("heiyingmail-gateway")
public interface CouponFeignService {
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);
    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo reductionTo);
}
