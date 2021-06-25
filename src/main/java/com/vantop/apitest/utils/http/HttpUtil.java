package com.vantop.apitest.utils.http;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

@Slf4j
public class HttpUtil {
    private static final HttpClientInit client=new HttpClientInit();

    public static HttpClientInit getClient(){
        return client;
    }

    public static String GetURL(String httpUrl,String Host, String token,String cookie) {
        // 创建get请求
        log.info("Start GET Request:"+httpUrl);
        log.info("token:\n"+token);
        HttpGet httpGet = new HttpGet(httpUrl);
        httpGet.setHeader("Connection","keep-alive");
        if(Host!=""){
            httpGet.setHeader("Host",Host);
        }
        if(token!=""){
            httpGet.setHeader("Authorization", token);
        }
        if(cookie!=""){
            httpGet.setHeader("cookie", cookie);
        }
        long nowTime = System.currentTimeMillis()+2;
        httpGet.setHeader("x-ca-reqid", nowTime +"."+(int)(Math.random()*1000)+1);
        httpGet.setHeader("x-ca-reqtime", String.valueOf(nowTime));
        return client.sendGet(httpGet);
    }

    public static String senddelete(String httpUrl,String Host, String token,String cookie) {
        // 创建get请求
        log.info("Start GET Request:"+httpUrl);
        log.info("token:\n"+token);
        HttpDelete httpDelete = new HttpDelete(httpUrl);
//        httpDelete.setHeader("Connection","keep-alive");
        if(Host!=""){
            httpDelete.setHeader("Host",Host);
        }
        if(token!=""){
            httpDelete.setHeader("Authorization", token);
        }
        if(cookie!=""){
            httpDelete.setHeader("cookie", cookie);
        }
        long nowTime = System.currentTimeMillis()+2;
        httpDelete.setHeader("x-ca-reqid", nowTime +"."+(int)(Math.random()*1000)+1);
        httpDelete.setHeader("x-ca-reqtime", String.valueOf(nowTime));
        return client.sendDelete(httpDelete);
    }


    /**
     * post发送文件
     * */
//    public static String PostFile(String httpUrl, Map<String, String> maps, List<File> fileLists) {
//        log.info("正在发送POST（上传文件）请求...");
//        // 创建httpPost
//        HttpPost httpPost = new HttpPost(httpUrl);
//        MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
//        if (maps != null) {
//            for (String key : maps.keySet()) {
//                meBuilder.addPart(key, new StringBody(maps.get(key), ContentType.TEXT_PLAIN));
//            }
//        }
//        if (fileLists != null) {
//            for (File file : fileLists) {
//                FileBody fileBody = new FileBody(file);
//                meBuilder.addPart("files", fileBody);
//            }
//        }
//        HttpEntity reqEntity = meBuilder.build();
//        httpPost.setEntity(reqEntity);
//        return client.sendPost(httpPost);
//    }

    /**
     * post发送json
     * */
    public static String PostJson(String httpUrl, String paramsJson, String Host, String token, String cookie) {
        log.info("Start POST（Jason） Request:"+httpUrl);
        log.info("RequestData:"+paramsJson);
        log.info("token:\n"+token);
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        //设置头信息
        long nowTime = System.currentTimeMillis()+2;
//        httpPost.setHeader("Connection","keep-alive");
        httpPost.setHeader("x-ca-reqid", nowTime +"."+(int)(Math.random()*1000)+1);
        httpPost.setHeader("x-ca-reqtime", String.valueOf(nowTime));
        if(Host!=""){
            httpPost.setHeader("Host",Host);
        }
        if(token!=""){
            httpPost.setHeader("Authorization", token);
        }
        if(cookie!=""){
            httpPost.setHeader("cookie", cookie);
        }
        try {
            // 设置json参数
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(HttpConfig.CONTENT_TYPE_JSON_URL);
                httpPost.setEntity(stringEntity);
            }else {
                log.error("请求参数为空...");
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return client.sendPost(httpPost);
    }

    /**
     * post发送json
     * */
    public static String PutJson(String httpUrl, String paramsJson, String Host, String token, String cookie) {
        log.info("Start PUT（Jason） Request:"+httpUrl);
        log.info("RequestData:"+paramsJson);
        log.info("token:\n"+token);
        // 创建httpPost
        HttpPut httpPut = new HttpPut(httpUrl);
        //设置头信息
        long nowTime = System.currentTimeMillis()+2;
//        httpPut.setHeader("Connection","keep-alive");
        httpPut.setHeader("x-ca-reqid", nowTime +"."+(int)(Math.random()*1000)+1);
        httpPut.setHeader("x-ca-reqtime", String.valueOf(nowTime));
        if(Host!=""){
            httpPut.setHeader("Host",Host);
        }
        if(token!=""){
            httpPut.setHeader("Authorization", token);
        }
        if(cookie!=""){
            httpPut.setHeader("cookie", cookie);
        }
        try {
            // 设置json参数
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(HttpConfig.CONTENT_TYPE_JSON_URL);
                httpPut.setEntity(stringEntity);
            }else {
                log.error("请求参数为空...");
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return client.sendPut(httpPut);
    }

    /**
     * 登录专业，用于获取请求头信息
     * */
    public static CloseableHttpResponse PostLogin(String httpUrl, String paramsJson, String Host) {
        log.info("Start POST（Jason） Request:"+httpUrl);
        log.info("RequestData:"+paramsJson);
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        //设置头信息
        long nowTime = System.currentTimeMillis()+2;
//        httpPost.setHeader("Connection","keep-alive");
        httpPost.setHeader("x-ca-reqid", nowTime +"."+(int)(Math.random()*1000)+1);
        httpPost.setHeader("x-ca-reqtime", String.valueOf(nowTime));
        if(Host!=null){
            httpPost.setHeader("Host",Host);
        }
        try {
            // 设置json参数
            if (paramsJson != null && paramsJson.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
                stringEntity.setContentType(HttpConfig.CONTENT_TYPE_JSON_URL);
                httpPost.setEntity(stringEntity);
            }else {
                log.error("请求参数为空...");
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return client.postBackHttpResponse(httpPost);
    }

    /**
     * post发送xml
     * */
    public static String PostXml(String httpUrl, String paramsXml) {
        log.info("开始发送POST(XML)请求...");
        // 创建httpPost
        HttpPost httpPost = new HttpPost(httpUrl);
        try {
            // 设置参数
            if (paramsXml != null && paramsXml.trim().length() > 0) {
                StringEntity stringEntity = new StringEntity(paramsXml, "UTF-8");
                stringEntity.setContentType(HttpConfig.CONTENT_TYPE_TEXT_HTML);
                httpPost.setEntity(stringEntity);
            }else {
                log.error("请求参数为空...");
            }
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        }
        return client.sendPost(httpPost);
    }




    public static void main(String[] args) throws IOException {
        String url = "http://172.24.14.70:80/auth/login";
        String data = "{\"account\":\"13534173939\",\"password\":\"Td147852\",\"captcha\":\"2222\"}";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000).setConnectionRequestTimeout(35000).setSocketTimeout(60000).build();
        httpPost.setConfig(requestConfig);

        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("DataEncoding", "UTF-8");
        httpPost.setHeader("Host", "api.nextop.cc");

        CloseableHttpResponse response = null;
        httpPost.setEntity(new StringEntity(data));

        response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        //可以获得响应头
        Header[] headers = response.getHeaders("Set-Cookie");
        for (Header header : headers) {
            if(header.getValue().startsWith("SESSION")){
                log.info("::: "+header.getValue().split(";")[0]);
            }
        }
        // 判断响应状态
        String responseContent = "";
        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
            responseContent = EntityUtils.toString(entity, HttpConfig.CHARSET_UTF_8);
            log.info("[Response]:" + responseContent);
            EntityUtils.consume(entity);
        }
        try {
            // 释放资源
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String token = "bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIwOThmNmJjZDQ2MjFkMzczY2FkZTRlODMyNjI3YjRmNiIsImlhdCI6MTYyMDkxMjkyOSwiYXVkIjoibmV4dG9waXVzZXIiLCJ0ZW5hbnRJZCI6IjE2MDczMzYzODQ3MzAiLCJ1aWQiOiI4NTQxOTc2NTczNjI3NTk2OCIsImV4cCI6MTYyMDk5OTMyOSwibmJmIjoxNjIwOTEyOTI5fQ.3zn2jgumTCWdvAC4oOEqbvwerkFe1AP5hTunC2HytyA";
//        String url = "http://172.24.14.70:80/purchase/plan/page";

//        HttpGet httpGet = new HttpGet(url);
//        httpGet.setHeader("Connection","keep-alive");
//        httpGet.setHeader("Host","api.nextop.cc");
//
//        httpGet.setHeader("Authorization", token);

//        long nowTime = System.currentTimeMillis()+2;
//        log.info(String.valueOf(nowTime));
//        httpGet.setHeader("x-ca-reqid", nowTime +"."+(int)(Math.random()*1000)+1);
//        httpGet.setHeader("x-ca-reqtime", String.valueOf(nowTime));
//        System.out.println(client.sendGet(httpGet));

//        HttpPost httpPost = new HttpPost(url);
//        String paramsJson = "{\"planCode\":\"\",\"current\":1,\"size\":20,\"approverIdList\":[],\"buyerIds\":[],\"requireUserIds\":[],\"platformIdList\":[],\"isPurchaseNumSort\":false,\"isAsk\":false}";
//        httpPost.setHeader("x-ca-reqid", nowTime +"."+(int)(Math.random()*1000)+1);
//        httpPost.setHeader("x-ca-reqtime", String.valueOf(nowTime));
//        httpPost.setHeader("Authorization", token);
//        httpPost.setHeader("Host","api.nextop.cc");
//        httpPost.setHeader("cookie","SESSION=YzMyODgwNWEtMjE3MS00M2RhLWJjNjAtZmFkMDgxNDI4MDI3");
//        StringEntity stringEntity = new StringEntity(paramsJson, "UTF-8");
//        stringEntity.setContentType(HttpConfig.CONTENT_TYPE_JSON_URL);
//        httpPost.setEntity(stringEntity);
//        System.out.println(client.sendPost(httpPost));

    }

}
