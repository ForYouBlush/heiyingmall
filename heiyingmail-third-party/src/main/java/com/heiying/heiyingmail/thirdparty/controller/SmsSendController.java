package com.heiying.heiyingmail.thirdparty.controller;

import com.heiying.common.utils.R;
import com.heiying.heiyingmail.thirdparty.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Resource
    SmsComponent smsComponent;
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        smsComponent.sendCode(phone,code);
        return R.ok();
    }
}
