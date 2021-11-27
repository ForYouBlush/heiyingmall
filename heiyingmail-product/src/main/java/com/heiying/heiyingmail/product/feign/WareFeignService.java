package com.heiying.heiyingmail.product.feign;

import com.heiying.common.utils.R;
import com.heiying.heiyingmail.product.vo.SkuHasStockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("heiyingmail-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
