package com.vantop.apitest.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.parsers.DynamicTableNameParser;
import com.baomidou.mybatisplus.extension.parsers.ITableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import com.vantop.apitest.common.SetTableName;
import com.vantop.apitest.vo.CurrentUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
@MapperScan(value = "com.vantop.apitest.mapper")
public class MybatisPlusConfig {

    Map<HttpServletRequest, Integer> pMaps = new ConcurrentHashMap<>();

    /**
     * 分页插件
     * @return 分页插件的实例
     */

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //分页配置
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));

        //动态表名配置
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        HashMap<String, TableNameHandler> map = new HashMap<String, TableNameHandler>(2) {{
//            put("mingfeng_test_account", (sql, tableName) -> {
//                //平台信息
//                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//                CurrentUserVO user = (CurrentUserVO) request.getSession().getAttribute("user");
//                Integer platform = user.getPlatform();
//                if(null!=platform){
//                    return SetTableName.setTable(platform, "test_account");
//                }
//                return tableName;
//            });
//
//            put("mingfeng_config_kind", (sql, tableName) -> {
//                //平台信息
//                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//                CurrentUserVO user = (CurrentUserVO) request.getSession().getAttribute("user");
//                Integer platform = user.getPlatform();
//                if(null!=platform){
//                    return SetTableName.setTable(platform, "config_kind");
//                }
//                return tableName;
//            });

            //涉及表集合
            List<String> tables = SetTableName.getTableName();

            //动态表规则 平台+_+表名
            tables.forEach(tableTitle -> put(tableTitle, (sql, tableName) -> {
                //平台信息
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                Integer platform;
                if(pMaps.get(request)!=null){
                    platform = pMaps.get(request);
                }else {
                    CurrentUserVO user = (CurrentUserVO) request.getSession().getAttribute("user");
                    platform = user.getPlatform();
                }
                if(null!=platform){
                    pMaps.put(request,platform);
                    return SetTableName.setTable(platform, tableTitle.replace("mingfeng_",""));
                }
                return tableName;
            }));
        }};
        dynamicTableNameInnerInterceptor.setTableNameHandlerMap(map);
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);

        return interceptor;
    }

    /**
     * 乐观锁插件
     * @return 乐观锁插件的实例
     */
//    @Bean
//    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
//        return new OptimisticLockerInterceptor();
//    }
}
