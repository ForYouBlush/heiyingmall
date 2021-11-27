package com.heiying.heiyingmail.order.feign;

import com.heiying.heiyingmail.order.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("heiyingmail-member")
public interface MemberFeignService {
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
     List<MemberAddressVO> getAddress(@PathVariable("memberId") Long memberId);
}
