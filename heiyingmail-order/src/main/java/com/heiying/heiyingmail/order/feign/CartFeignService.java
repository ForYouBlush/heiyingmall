package com.heiying.heiyingmail.order.feign;

import com.heiying.heiyingmail.order.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("heiyingmail-cart")
public interface CartFeignService {
    @GetMapping("/currentUserItems")
    @ResponseBody
    List<OrderItemVO> getCurrentUserItems();
}
