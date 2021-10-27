package com.heiying.heiyingmail.product.exception;

import com.heiying.common.exception.BizCodeEnume;
import com.heiying.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestControllerAdvice(basePackages = "com.heiying.heiyingmail.product.controller")
public class HeiyingmailExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R hanleVaildException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errorMap=new HashMap<>();
        bindingResult.getFieldErrors().forEach((item)->{
            errorMap.put(item.getField(),item.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(), BizCodeEnume.VAILD_EXCEPTION.getMsg()).put("data",errorMap);

    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        throwable.printStackTrace();
        log.error("错误{},{}",throwable.getClass(),throwable.getMessage());
        return  R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
