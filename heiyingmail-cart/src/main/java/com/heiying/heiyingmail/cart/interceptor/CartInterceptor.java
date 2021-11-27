package com.heiying.heiyingmail.cart.interceptor;

import com.heiying.common.constant.AuthServerConstant;
import com.heiying.common.constant.CartConstant;
import com.heiying.common.vo.MemberRespVO;
import com.heiying.heiyingmail.cart.to.UserInfoTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法之前，判断用户的登录状态。并封装传递给controller目标请求
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTO> threadLocal=new ThreadLocal<>();

    /**
     * 业务执行之前，保存临时用户
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        UserInfoTO userInfoTO = new UserInfoTO();
        HttpSession session = request.getSession();
        MemberRespVO vo = (MemberRespVO) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (vo!=null){
            //用户登陆
            userInfoTO.setUserId(vo.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTO.setUserKey(cookie.getValue());
                    userInfoTO.setTempUser(true);
                }
            }
        }
        //如果没有临时用户一定要分配一个临时用户
        if (StringUtils.isEmpty(userInfoTO.getUserKey())){
            String s = UUID.randomUUID().toString();
            userInfoTO.setUserKey(s);
        }

        threadLocal.set(userInfoTO);
        return true;
    }

    /**
     *  业务执行之后，分配临时用户，保存cookie
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTO userInfoTO = threadLocal.get();
        if (!userInfoTO.getTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTO.getUserKey());
            cookie.setDomain("heiyingmail.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}
