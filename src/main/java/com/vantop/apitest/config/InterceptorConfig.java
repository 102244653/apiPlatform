package com.vantop.apitest.config;

import com.vantop.apitest.intercepors.SessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration //定义为配置类，相当于xml配置文件
//public class InterceptorConfig implements WebMvcConfigurer {
public class InterceptorConfig extends WebMvcConfigurerAdapter{

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        // 需要登陆
        String[] addPathPatterns = {"/va/**", "/index","/index0", "/main", "/logout", "/single/**","/flow/**","/system/**","/swagger/**"};
        //不需要登陆
        String[] excludePathPatterns = {"/va/login","/va/mock", "", "/va/register", "/favicon.ico", "/local/**", "/layui/**"};

        // 注册拦截器对象：SessionInterceptor
        registry.addInterceptor(new SessionInterceptor()).addPathPatterns(addPathPatterns).excludePathPatterns(excludePathPatterns);
    }

    /**
     * 解决resources下面静态资源无法访问
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/")//favicon.ico
                .addResourceLocations("classpath:/static/");
        super.addResourceHandlers(registry);
    }
}
