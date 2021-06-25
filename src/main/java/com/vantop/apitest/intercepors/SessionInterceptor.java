package com.vantop.apitest.intercepors;

import com.vantop.apitest.utils.DESUtil;
import com.vantop.apitest.vo.CurrentUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class SessionInterceptor implements HandlerInterceptor {
    /**
     * 给视图类【@controller】注册一个拦截器
     * 然后通过配置类加载使用
     * */

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        return;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        return;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        CurrentUserVO user = (CurrentUserVO) session.getAttribute("user");
        if(null == user || !this.isLogin(user)){
            //redirect(request, response);
            response.sendRedirect(request.getContextPath()+"/login");
            return false;
        }
        return true;
    }

    public boolean isLogin(CurrentUserVO user){
        //解析token
        String info =  DESUtil.decrypt(user.getToken());
        //token详情
        String[] detail = info.split(";");
        // uid一致且登陆时间小于7*24H
        if(detail[0].equals(String.valueOf(user.getUid())) ){//&& (new DESUtil().compareTime(detail[1]))
            return true;
        }else {
            return false;
        }
    }

    //对于请求是ajax请求重定向问题的处理方法
//    public void redirect(HttpServletRequest request, HttpServletResponse response) throws  IOException {
//        //获取当前请求的路径
//        String basePath = request.getScheme() + "://" + request.getServerName() + ":"  +
//                request.getServerPort()+request.getContextPath();
//        //如果request.getHeader("X-Requested-With") 返回的是"XMLHttpRequest"说明就是ajax请求，需要特殊处理 否则直接重定向就可以了
//        if("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))){
//            //告诉ajax本次拦截为重定向
//            response.setHeader("REDIRECT", "REDIRECT");
//            //告诉ajax重定向路径
//            response.setHeader("CONTENTPATH", basePath+"/login");
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        }else{
//            response.sendRedirect(basePath + "/login");
//        }
//    }
}
