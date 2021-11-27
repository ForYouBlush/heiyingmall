package com.heiying.heiyingmail.seckill.controller;

import com.heiying.common.utils.R;
import com.heiying.heiyingmail.seckill.service.SeckillService;
import com.heiying.heiyingmail.seckill.to.SeckillSkuRedisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;


    /**
     * 返回当前时间可以参与秒杀的商品信息
     *
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTO> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    /**
     * 获取秒杀商品信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTO to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    /**
     * 秒杀商品
     * @param killId
     * @param code
     * @param num
     * @param model
     * @return
     */
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String code,
                          @RequestParam("num") Integer num, Model model) {
        String orderSn = seckillService.kill(killId, code, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
