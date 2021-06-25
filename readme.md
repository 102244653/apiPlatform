    --vantoptest
        --logs                  日志
        --notes                 本地文件
        --src/main/java/com/vantop/apitest
            --common            公共方法
            --config            全局配置
            --flow              业务流用例模块
            --interceports      全局拦截器
            --mapper            mapper
            --schedule          定时任务
            --single            单接口用例模块
            --swagger           swagger解析模块
            --system            系统设置【环境、规则配置】
        
            --user              用户管理
                --model         实体类
                --controller    模块对应接口
                --service       模块对应服务
                
            --vo                vo
        --src/main/resources
            --static            静态资源
            --templates         html页面
            
            --application.properties    springboot配置：修改dev/pro即可
            --application-pro.properties        正是环境配置
            --application-dev.properties        开发环境配置
            