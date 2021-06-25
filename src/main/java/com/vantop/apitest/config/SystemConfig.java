package com.vantop.apitest.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
public class SystemConfig {

    /**
     * 系统设置：
     * 方法一：设置字符集=utf-8
     * 方法二：核心文件配置
     * */

    public FilterRegistrationBean characterEncodingFilterRegistrationBean(){
        //创建字符编码过滤器
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();

        //设置强制使用指定编码
        characterEncodingFilter.setForceEncoding(true);

        //设置指定编码
        characterEncodingFilter.setEncoding("utf-8");

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();

        //设置支持编码过滤器
        filterRegistrationBean.setFilter(characterEncodingFilter);
        //设置过滤路径：* =》所有
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }

}
