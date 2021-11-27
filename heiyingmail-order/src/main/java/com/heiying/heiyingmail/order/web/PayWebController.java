package com.heiying.heiyingmail.order.web;

import com.alipay.api.AlipayApiException;
import com.heiying.heiyingmail.order.config.AlipayTemplate;
import com.heiying.heiyingmail.order.service.OrderService;
import com.heiying.heiyingmail.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    OrderService orderService;


    @GetMapping(value = "/orderPay",produces = "text/html")
    @ResponseBody
    public String payOrder(@RequestParam("orderSn")String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        //支付宝返回的是一个页面，将此页面直接交给浏览器
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }
}
