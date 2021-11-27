package com.heiying.heiyingmail.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.heiying.common.constant.AuthServerConstant;
import com.heiying.common.exception.BizCodeEnume;
import com.heiying.common.utils.R;
import com.heiying.heiyingmail.auth.feign.MemberFeignService;
import com.heiying.heiyingmail.auth.feign.ThirdPartyFeignService;
import com.heiying.common.vo.MemberRespVO;
import com.heiying.heiyingmail.auth.vo.UserLoginVO;
import com.heiying.heiyingmail.auth.vo.UserRegistVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    /**
     *  实现WebMvcConfigurer代替这些空方法（发送一个请求直接跳转到一个页面）
     */
    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        if (session.getAttribute(AuthServerConstant.LOGIN_USER)!=null){
            return "redirect:http://heiyingmail.com";
        };
        return "login";
    }
//
//    @GetMapping("/reg.html")
//    public String regPage(){
//        return "reg";
//    }

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;


    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        //1、接口防刷


        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(s)){
            long l = Long.parseLong(s.split("_")[1]);
            if(l-System.currentTimeMillis()<60*1000){
                //60秒内不能在发送
                return R.error(BizCodeEnume.VAILD_SMS_EXCEPTION.getCode(),BizCodeEnume.VAILD_EXCEPTION.getMsg());
            }
        }

        //2、验证码的再次校验，redis key-phone，value-code
        String code = UUID.randomUUID().toString().substring(0, 5)+"_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,10, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone,code.split("_")[0]);
        return R.ok();
    }

    /**
     *      TODO 重定向携带数据是用session原理，将数据放在session中。
     *      只要跳到下一个页面取出数据以后，session中的数据就会删掉
     *
     *      //TODO 1、分布式下的session问题
     *
     * @param vo
     * @param result
     * @param redirectAttributes    模拟重定向携带数据
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVO vo, BindingResult result
            , RedirectAttributes redirectAttributes){
        if (result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
//            model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors",errors);
            //校验出错转发到注册页,转发请求默认是get，不能通过post转发
//            return "forward:/reg.html";
            return "redirect:http://auth.heiyingmail.com/reg.html";
        }


        //1、校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)){
            if (code.equals(s.split("_")[0])){
                //验证码通过后删除验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                //验证码通过    真正的注册，调用远程服务进行注册
                R regist = memberFeignService.regist(vo);
                if (regist.getCode()==0){
                    //成功
                    return "redirect:http://auth.heiyingmail.com/login.html";
                }else{
                    //失败
                    Map<String, String> errors=new HashMap<>();
                    errors.put("msg",regist.getData("data",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.heiyingmail.com/reg.html";
                }

            }else{
                //验证码不正确
                Map<String, String> errors=new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute(errors);
                return "redirect:http://auth.heiyingmail.com/reg.html";
            }
        }else{
            //验证码不正确
            Map<String, String> errors=new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute(errors);
            return "redirect:http://auth.heiyingmail.com/reg.html";
        }
    }


    @PostMapping("/login")
    public String login(UserLoginVO vo, RedirectAttributes redirectAttributes,
                        HttpSession session){
        //远程登录
        R login = memberFeignService.login(vo);
        if (login.getCode()==0){
            //登录成功，放到session中
            MemberRespVO data = login.getData("data", new TypeReference<MemberRespVO>() {
            });
            //TODOd 1、session保存的作用域是当前域名，子域不能获取
            //TODOd 2、序列化到redis采用的是jdk，如何使用JSON的方式存储到redis
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://heiyingmail.com";
        }else {
            Map<String, String> errors=new HashMap<>();
            String data = login.getData("msg", new TypeReference<String>() {
            });
            errors.put("msg",data);
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.heiyingmail.com/login.html";
        }
    }

}
