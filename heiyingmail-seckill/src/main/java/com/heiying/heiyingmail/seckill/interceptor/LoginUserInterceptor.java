package com.heiying.heiyingmail.seckill.interceptor;

import com.heiying.common.constant.AuthServerConstant;
import com.heiying.common.vo.MemberRespVO;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<MemberRespVO> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean match = matcher.match("/kill", uri);
        if (match) {
            MemberRespVO attribute = (MemberRespVO) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (attribute != null) {
                loginUser.set(attribute);
                return true;
            } else {
                request.getSession().setAttribute("msg", "请先登录");
                response.sendRedirect("http://auth.heiyingmail.com/login.html");
                return false;
            }
        }
        return true;

    }
}
