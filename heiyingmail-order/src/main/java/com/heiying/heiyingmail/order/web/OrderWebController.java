package com.heiying.heiyingmail.order.web;

import com.heiying.heiyingmail.order.service.OrderService;
import com.heiying.heiyingmail.order.vo.OrderConfirmVO;
import com.heiying.heiyingmail.order.vo.OrderSubmitVO;
import com.heiying.heiyingmail.order.vo.SubmitOrderRespVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 跳转到订单确认页
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVO confirmVO=orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVO);
        return "confirm";
    }

    /**
     * 提交订单
     * @param vo
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVO vo, Model model, RedirectAttributes attributes){
//        System.out.println("订单提交的数据"+vo);
        SubmitOrderRespVO respVO=orderService.submitOrder(vo);
        if (respVO.getCode()==0){
            //下单成功-》跳转支付选择页
            model.addAttribute("submitOrderResp",respVO);
            return "pay";
        }else {
            //下单失败
            String msg="下单失败：";
            switch (respVO.getCode()){
                case 1:msg+="订单信息过期，请刷新再次提交"; break;
                case 2:msg+="订单商品价格发生变化，请确认后再次提交";break;
                case 3:msg+="商品库存不足";break;
            }
            attributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.heiyingmail.com/toTrade";
        }

    }


}
