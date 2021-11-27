package com.heiying.heiyingmail.cart.config;

import com.heiying.heiyingmail.cart.interceptor.CartInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class HeiyingmailWebConfig implements WebMvcConfigurer {

    @Autowired
    CartInterceptor cartInterceptor;



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/cartList.html").setViewName("cartList");
        registry.addViewController("/success.html").setViewName("success");
    }
}
