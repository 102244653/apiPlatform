package com.vantop.apitest.utils.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;


import java.io.IOException;

@Slf4j
public class HttpClientInit {

    // 创建默认的httpClient实例.
    static CloseableHttpClient httpClient = HttpSetting.getHttpClient();

    /**
     * Post请求
     */
    public  String sendPost(HttpPost httpPost) {
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 配置请求信息
            httpPost.setConfig(HttpSetting.requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            // 得到响应实例
            HttpEntity entity = response.getEntity();
            // 判断响应状态
            int Statuscode=response.getStatusLine().getStatusCode();
            log.info("Statuscode："+Statuscode);
            if ( Statuscode>= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + Statuscode);
            }
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, HttpConfig.CHARSET_UTF_8);
                log.info("[Response]:"+responseContent);
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
        return responseContent;
    }

    /**
     * Put请求
     */
    public  String sendPut(HttpPut httpPut) {
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 配置请求信息
            httpPut.setConfig(HttpSetting.requestConfig);
            // 执行请求
            response = httpClient.execute(httpPut);
            // 得到响应实例
            HttpEntity entity = response.getEntity();
            // 判断响应状态
            int Statuscode=response.getStatusLine().getStatusCode();
            log.info("Statuscode："+Statuscode);
            if ( Statuscode>= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + Statuscode);
            }
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, HttpConfig.CHARSET_UTF_8);
                log.info("[Response]:"+responseContent);
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
        return responseContent;
    }

    /**
     * Delete请求
     */
    public  String sendDelete(HttpDelete httpDelete) {
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 配置请求信息
            httpDelete.setConfig(HttpSetting.requestConfig);
            // 执行请求
            response = httpClient.execute(httpDelete);
            // 得到响应实例
            HttpEntity entity = response.getEntity();
            // 判断响应状态
            int Statuscode=response.getStatusLine().getStatusCode();
            log.info("Statuscode："+Statuscode);
            if ( Statuscode>= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + Statuscode);
            }
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, HttpConfig.CHARSET_UTF_8);
                log.info("[Response]:"+responseContent);
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
        return responseContent;
    }

    /**
     * 发送Get请求
     * @return
     */
    public String sendGet(HttpGet httpGet) {
        CloseableHttpResponse response = null;
        // 响应内容
        String responseContent = null;
        try {
            // 配置请求信息
            httpGet.setConfig(HttpSetting.requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            // 得到响应实例
            HttpEntity entity = response.getEntity();

            // 判断响应状态
            int statuscode=response.getStatusLine().getStatusCode();
            log.info("Statuscode："+statuscode);
            if (statuscode >= 300) {
                log.error("响应异常，状态码："+statuscode);
                throw new Exception("HTTP Request is not success, Response code is " + statuscode);
            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                responseContent = EntityUtils.toString(entity, HttpConfig.CHARSET_UTF_8);
                log.info("[Response]:"+responseContent);
                EntityUtils.consume(entity);
            }

        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            try {
                // 释放资源
                if (response != null) {
                    log.info("请求完成，释放资源...");
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
        return responseContent;
    }


    //返回HttpResponse，可以拿到请求头信息
    public CloseableHttpResponse postBackHttpResponse(HttpPost httpPost) {
        CloseableHttpResponse response = null;
        try {
            // 配置请求信息
            httpPost.setConfig(HttpSetting.requestConfig);
            // 执行请求
            response = httpClient.execute(httpPost);
            // 判断响应状态
            int Statuscode = response.getStatusLine().getStatusCode();
            log.info("Statuscode："+Statuscode);
            if ( Statuscode>= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + Statuscode);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return response;
    }

}
