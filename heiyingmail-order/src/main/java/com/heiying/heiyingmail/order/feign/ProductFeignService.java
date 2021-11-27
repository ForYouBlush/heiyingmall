package com.heiying.heiyingmail.order.feign;

import com.heiying.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient("heiyingmail-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/skuId/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
