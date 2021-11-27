package com.heiying.heiyingmail.member.web;

import com.alibaba.fastjson.JSON;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.member.fegin.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
@Controller
public class MemberWebController {
    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/orderList.html")
    public String orderList(@RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum, Model model){
        Map<String,Object> map=new HashMap<>();
        map.put("pageNum",pageNum.toString());
        R r = orderFeignService.listWithItem(map);
//        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders",r);
        return "orderList";
    }


}
