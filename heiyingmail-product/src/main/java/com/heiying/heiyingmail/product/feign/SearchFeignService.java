package com.heiying.heiyingmail.product.feign;

import com.heiying.common.to.es.SkuEsModel;
import com.heiying.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("heiyingmail-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
     R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
