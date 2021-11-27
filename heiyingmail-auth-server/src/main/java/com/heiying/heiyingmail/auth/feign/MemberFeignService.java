package com.heiying.heiyingmail.auth.feign;

import com.heiying.common.utils.R;
import com.heiying.heiyingmail.auth.vo.UserLoginVO;
import com.heiying.heiyingmail.auth.vo.UserRegistVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("heiyingmail-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVO userRegistVO);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVO vo);
}
