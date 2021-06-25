package com.vantop.apitest.swagger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vantop.apitest.utils.TimeUtils;
import com.vantop.apitest.mapper.*;
import com.vantop.apitest.single.model.SingleCase;
import com.vantop.apitest.swagger.model.BlackList;
import com.vantop.apitest.swagger.model.Swagger;
import com.vantop.apitest.swagger.model.SwaggerLog;
import com.vantop.apitest.system.model.ConfigKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class SwaggerService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    SwaggerMapper swaggerMapper;

    @Autowired
    SwaggerLogMapper swaggerLogMapper;

    @Autowired
    BlackListMapper blackListMapper;

    @Autowired
    ConfigKindMapper configKindMapper;

    @Autowired
    SingleCaseMapper singleCaseMapper;

    public void requestSwagger(Integer kindID) {

        /**
         *根据config_kind的一级分类解析swagger地址
         * */
        String body = "";
        log.info("Swagger开始解析");

        List<ConfigKind> swaggerUrl = configKindMapper.selectList(null);
        for (ConfigKind kind : swaggerUrl) {
            if(kindID != 0 && kind.getKindId() != kindID){
                continue;
            }
            if (kind.getSwaggerUrl() == null || kind.getStatus() == -1) {
                continue;
            }
            log.info("当前Swagger解析地址：" + kind.getSwaggerUrl());
            ResponseEntity<String> forEntity = restTemplate.getForEntity(kind.getSwaggerUrl(), String.class);
            //获取响应的状态
            HttpStatus statusCode = forEntity.getStatusCode();
            if (statusCode.value() != 200) {
                log.error(kind.getKindName() + "模块swagger地址请求失败！");
                continue;
            }
            try {
                //获取响应的body信息
                body = forEntity.getBody();
                JSONObject swaggerObject = JSON.parseObject(body);
                JSONObject paths = swaggerObject.getJSONObject("paths");
                JSONObject definitions = swaggerObject.getJSONObject("definitions");
                this.ReadApiDetail(kind, paths, definitions);
            } catch (Exception e) {
                log.error(kind.getKindName() + "[URL::" + kind.getSwaggerUrl() + "]" + " 模块swagger解析错误！\n" + e.getMessage());
            }
        }
        log.info("Swagger全部解析完毕");
    }

    public void ReadApiDetail(ConfigKind kind, JSONObject paths, JSONObject definitions) {
        /**
         * 将swagger解析成swagger对象list
         * */

        Iterator<String> iterator = paths.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            log.info("==============模块：" + kind.getKindName() + ",接口：" + name + "===============");
            JSONObject value = paths.getJSONObject(name);
            String method0 = "";
            try {
                for (Map.Entry<String, Object> entry : value.entrySet()) { //遍利paths
                    Swagger ss = new Swagger();
                    ss.setKind(kind.getKindId());  //模块
                    ss.setName(name);// 接口地址
                    String method = entry.getKey();
                    method0 = method;
                    if (!(method.equals("post") || method.equals("get") || method.equals("put") || method.equals("delete"))) {
                        log.info("当前不支持该请求方式：" + method);
                        continue;
                    }
                    ss.setMethod(entry.getKey()); // 请求类型
                    JSONObject detail = (JSONObject) entry.getValue();  //接口详情

                    if (detail.containsKey("summary")) { //接口名称
                        ss.setTag(detail.getString("summary"));
                    } else {
                        ss.setTag(name);
                    }

                    if (detail.containsKey("consumes")) { //请求头
                        ss.setHeader(detail.getJSONArray("consumes").toString());
                    } else {
                        ss.setHeader("");
                    }

                    if (detail.containsKey("responses")) { // 读取正确的响应结果
                        ss.setResponses(this.analysisResponses(detail, definitions).toString());
                    }

                    //解析接口参数
                    if (detail.containsKey("parameters")) {
                        //解析接口参数
                        ss.setParameters(this.analysisParameters(detail, definitions).toString());
                        //解析成json，改为不推送请求参数
//                        ss.setParams(this.analysisPara(entry.getKey(), detail, definitions).toString());
                    } else {
                        //无请求参数
                        ss.setParameters("{\"keyList\": [], \"definitions\": {}}");
                    }
                    this.updateSwaggerApi(ss);
                }

            } catch (Exception e) {
                e.printStackTrace();
                SwaggerLog sl = new SwaggerLog();
                sl.setKindName(kind.getKindName());
                sl.setName(kind.getBase()+name);
                sl.setRecord(value.toString());
                sl.setMethod(method0);
                sl.setUpdateTime(TimeUtils.nowTime());
                swaggerLogMapper.insert(sl);
            }
        }
    }

    public JSONObject analysisParameters(JSONObject detail, JSONObject definitions) {

        /**
         * 解析接口参数
         * */
        JSONObject finaObj = new JSONObject();  //最终解析结果
        JSONObject myDefinition = new JSONObject(); //保存对象实体信息
        JSONArray keyList = new JSONArray(); //保存请求字段
        if (detail.containsKey("parameters")) {//有参数则解析，否则直接返回
            JSONArray parameters = detail.getJSONArray("parameters");

            for (int i = 0; i < parameters.size(); i++) {
                JSONObject parameter = new JSONObject(); //单个字段解析结果
                JSONObject obj = (JSONObject) parameters.get(i);
                parameter.put("in", obj.getString("in"));
                parameter.put("name", obj.getString("name"));
                if (obj.containsKey("description")) {
                    parameter.put("description", obj.getString("description"));
                }
                if (obj.containsKey("required")) {
                    parameter.put("required", String.valueOf(obj.getBooleanValue("required")));
                }

                if (obj.containsKey("schema")) { //判断参数是否是对象
                    JSONObject schema = obj.getJSONObject("schema");
                    if (schema.containsKey("type")) {
                        parameter.put("type", String.valueOf(obj.getString("type")));
                    }
                    String reqRef = "";
                    if (schema.containsKey("originalRef")) {
                        reqRef = schema.getString("originalRef");
                    } else if (schema.containsKey("items") && schema.getJSONObject("items").containsKey("originalRef")) {
                        reqRef = schema.getJSONObject("items").getString("originalRef");
                    }
                    if (!reqRef.equals("")) {
                        JSONObject def;
                        parameter.put("originalRef", reqRef); //遍历解析
                        log.info("当前解析对象：" + reqRef);
                        if (!definitions.containsKey(reqRef)) {
                            reqRef = reqRef.substring(0, 1).toUpperCase() + reqRef.substring(1);
                            if (!definitions.containsKey(reqRef)) {
                                def = null;
                            } else {
                                //根据key获取对象
                                def = definitions.getJSONObject(reqRef);
                            }
                        } else {
                            //根据key获取对象
                            def = definitions.getJSONObject(reqRef);
                        }
                        myDefinition.put(reqRef, def);
                    }
                } else if (obj.containsKey("type")) {
                    parameter.put("type", obj.getString("type"));
                }
                keyList.add(parameter);
            }
        }
        finaObj.put("keyList", keyList);
        finaObj.put("definitions", myDefinition);
        return finaObj;
    }

    public JSONObject analysisResponses(JSONObject detail, JSONObject definitions) {
        /**
         * 解析接口响应结果
         * */
        JSONObject finaResult = new JSONObject();  //最终解析结果
        JSONObject myDefinition = new JSONObject(); //保存对象实体信息
        JSONObject resultObject = new JSONObject(); //保存响应结果字段
        JSONObject status = detail.getJSONObject("responses");
        for (Map.Entry<String, Object> entry : status.entrySet()) { //遍利responses
            JSONObject myResponse = new JSONObject();
            String statusCode = entry.getKey();
            JSONObject detailRes = (JSONObject) entry.getValue();  //response详情
            myResponse.put("description", detailRes.getString("description"));
            if (detailRes.containsKey("schema")) {
                JSONObject schema = detailRes.getJSONObject("schema");
                if (schema.containsKey("type")) {
                    myResponse.put("type", schema.getString("type"));
                }
                String ref = "";
                if (schema.containsKey("originalRef")) {
                    ref = schema.getString("originalRef");
                } else if (schema.containsKey("items") && schema.getJSONObject("items").containsKey("originalRef")) {
                    ref = schema.getJSONObject("items").getString("originalRef");
                }
                if (!ref.equals("")) {
                    log.info("当前解析对象：" + ref);
                    myResponse.put("originalRef", ref);
                    myDefinition.put(ref, definitions.getJSONObject(ref));
                    JSONObject def;
                    if (!definitions.containsKey(ref)) {
                        ref = ref.substring(0, 1).toUpperCase() + ref.substring(1);
                        if (!definitions.containsKey(ref)) {
                            def = null;
                        } else {
                            //根据key获取对象
                            def = definitions.getJSONObject(ref);
                        }
                    } else {
                        //根据key获取对象
                        def = definitions.getJSONObject(ref);
                    }

                    myDefinition.put(ref, def);
                }
            }
            resultObject.put(statusCode, myResponse);
        }
        finaResult.put("responses", resultObject);
        finaResult.put("definitions", myDefinition);
        return finaResult;
    }

    public Object analysisPara(String requestType, JSONObject detail, JSONObject definitions) {
        /**
         * 把参数解析成json格式参数
         * */
        //最终返回的解析结果
        Object finalJson = " ";
        if (!detail.containsKey("parameters")) {
            return finalJson;
        }
        //初始请求参数
        JSONArray parameters = detail.getJSONArray("parameters");
        try {
            if (requestType.equals("get") || requestType.equals("delete")) {
                finalJson = "?";
                //get请求拼接方式： ?id=${【公司id】int}&name=${【公司名称】string}
                for (int i = 0; i < parameters.size(); i++) {
                    JSONObject para = (JSONObject) parameters.get(i);
                    try {
                        String name = para.getString("name");
                        String description = para.getString("description");
                        if (!finalJson.equals("?")) {
                            finalJson += "&";
                        }
                        finalJson += name + "={" + description + "}";
                    } catch (Exception e) {
                        log.error("解析参数失败：" + para);
                        e.printStackTrace();
                    }
                }
                if (finalJson.equals("?")) {
                    finalJson = "";
                }
            } else if (requestType.equals("post") || requestType.equals("put")) {
                JSONObject postPara = new JSONObject();
                for (int i = 0; i < parameters.size(); i++) {
                    JSONObject para = (JSONObject) parameters.get(i);
                    String description = para.getString("description");
                    String type = "string"; // 默认类型为string
                    if (para.containsKey("type")) {
                        type = para.getString("type");
                    }
                    //判断参数是否是对象
                    String reqRef = ""; //默认为空
                    if (para.containsKey("schema")) {
                        JSONObject schema = para.getJSONObject("schema");
                        //参数类型
                        if (schema.containsKey("type")) {
                            type = schema.getString("type");
                        }
                        //获取对象名称
                        if (schema.containsKey("originalRef")) {
                            reqRef = schema.getString("originalRef");
                        } else if (schema.containsKey("items") && schema.getJSONObject("items").containsKey("originalRef")) {
                            reqRef = schema.getJSONObject("items").getString("originalRef");
                        }
                    }

                    //验证实体对象是否存在
                    Boolean is_exist = true;
                    if (reqRef.equals("")) {
                        is_exist = false;
                    } else if (!definitions.containsKey(reqRef)) {
                        reqRef = reqRef.substring(0, 1).toUpperCase() + reqRef.substring(1);
                        if (!definitions.containsKey(reqRef)) {
                            is_exist = false;
                        }
                    }

                    //解析对象
                    if (is_exist) {
                        //根据key获取对象
                        JSONObject def = definitions.getJSONObject(reqRef);
                        //读取字段信息
                        JSONObject pro = def.getJSONObject("properties");
                        Iterator<String> iterator = pro.keySet().iterator();
                        //遍历字段信息
                        while (iterator.hasNext()) {
                            String keyName1 = iterator.next();
                            //读取字段名称
                            JSONObject keyValue1 = pro.getJSONObject(keyName1);
                            //判断字段是否包含对象实体名称，默认为空
                            String resRef1 = "";
                            if (keyValue1.containsKey("items") && keyValue1.getJSONObject("items").containsKey("originalRef")) {
                                resRef1 = keyValue1.getJSONObject("items").getString("originalRef");
                            } else if (keyValue1.containsKey("originalRef")) { //如果字段的值是个对象，递归解析
                                resRef1 = keyValue1.getString("originalRef");
                            }
                            type = keyValue1.containsKey("type") ? keyValue1.getString("type") : "string";

                            String value = keyValue1.getString("description");
                            if (!resRef1.equals("")) {
                                value = resRef1;
                            }
                            //字段不是對象
                            if (type.equals("array")) {
                                JSONArray keyArray1 = new JSONArray();
                                keyArray1.add(value);
                                postPara.put(keyName1, keyArray1);
                            } else {
                                postPara.put(keyName1, value);
                            }
                        }
                    } else {
                        //为空时则直接解析字段名
                        if (type.equals("array")) {
                            JSONArray keyArray = new JSONArray();
                            keyArray.add(description);
                            postPara.put(para.getString("name"), keyArray);
                        } else {
                            postPara.put(para.getString("name"), description);
                        }
                    }
                }
                finalJson = postPara;
            }
        } catch (Exception e) {
            log.info(detail.toString());
            e.printStackTrace();
        }
        return finalJson;
    }

    @Transactional
    public void updateSwaggerApi(Swagger swagger) {
        /**
         * 根据返回状态更新swagger和 mingfeng
         * */

        QueryWrapper<ConfigKind> queryConfig = new QueryWrapper<>();
        queryConfig.eq("kindId", swagger.getKind());
        ConfigKind configKind = configKindMapper.selectOne(queryConfig);
        String baseUrl = configKind.getBase();
        swagger.setName(baseUrl + swagger.getName());
        swagger.setUpdateTime(TimeUtils.nowTime());
        //过滤黑名单
        QueryWrapper<BlackList> queryBlackList = new QueryWrapper<>();
        queryBlackList.eq("name", swagger.getName()).eq("method", swagger.getMethod());
        BlackList bb = blackListMapper.selectOne(queryBlackList);
        if (!Objects.isNull(bb)) {
            return;
        }

        Swagger oldSwagger = this.getSwaggerByNameMethod(swagger.getName(), swagger.getMethod());
        if (oldSwagger == null) {
            //不存在则直接插入
            swagger.setStatus(1);
            swaggerMapper.insert(swagger);
        } else {
            //已存在则更新
            Swagger newSwagger = oldSwagger;
            newSwagger.setStatus(0);//恢复默认状态

            if (!oldSwagger.getTag().equals(swagger.getTag())) {
                newSwagger.setTag(swagger.getTag());
            }
            if (!oldSwagger.getHeader().equals(swagger.getHeader())) {
                newSwagger.setHeader(swagger.getHeader());
            }
            //请求参数变更
            //改为由mock数据推送
//            if (!oldSwagger.getParameters().equals(swagger.getParameters())) {
//                newSwagger.setParameters(swagger.getParameters());
//                newSwagger.setParams(swagger.getParams());
//                newSwagger.setStatus(2);
//            }
            //响应结果变更
            if (!oldSwagger.getResponses().equals(swagger.getResponses())) {
                newSwagger.setResponses(swagger.getResponses());
                if(newSwagger.getStatus()==2){
                    newSwagger.setStatus(4);
                }else {
                    newSwagger.setStatus(3);
                }
            }
            swaggerMapper.updateById(newSwagger);
        }
        //同步更新api基表
        this.updateApi(swagger);
    }

    @Transactional
    public void updateApi(Swagger swagger) {
        int method = 0;
        switch (swagger.getMethod()) {
            case "get":
                method = 1;
                break;
            case "put":
                method = 2;
                break;
            case "delete":
                method = 3;
                break;
        }

        QueryWrapper<SingleCase> querySingleCase = new QueryWrapper<>();
        querySingleCase.eq("name", swagger.getName()).eq("method", method);

        List<SingleCase> reqInfoOld = singleCaseMapper.selectList(querySingleCase);
        if (reqInfoOld.size() == 0) {
            //为空的时候直接插入
            SingleCase reqInfo = new SingleCase();
            reqInfo.setName(swagger.getName());
            reqInfo.setMethod(method);
            //从swagger自己获取
            reqInfo.setParams(swagger.getParams());
            reqInfo.setStatus(1);
            reqInfo.setTag(swagger.getTag());
            reqInfo.setKindDirId(swagger.getKind() * 100 + 1);
            singleCaseMapper.insert(reqInfo);
        } else {
            if (swagger.getStatus() != 0) {
                int status = swagger.getStatus();
                switch (status){
                    case 2:
                        status = 5;
                        break;
                    case 3:
                        status = 6;
                        break;
                    case 4:
                        status = 7;
                        break;
                    default:
                        break;
                }
                //存在则判断是否需要更新
                for (SingleCase req : reqInfoOld) {
                    req.setStatus(status);
                    //更改用例的状态为已变更
                    singleCaseMapper.updateById(req);
                }
            }
        }
    }

    // json有序遍历结果
    public HashMap<String, String> LinkJson(String json) {
        HashMap<String, String> linkJson = new HashMap<>();
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(json,
                new TypeReference<LinkedHashMap<String, String>>() {
                });
        for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
            linkJson.put(entry.getKey(), entry.getValue());
        }
        return linkJson;
    }

    /**
     * 读取swaggerlist列表
     * */
    public Swagger getSwaggerByNameMethod(String name, String method){
        QueryWrapper<Swagger> querySwagger = new QueryWrapper<>();
        querySwagger.eq("name", name).eq("method", method);
        Swagger swagger = swaggerMapper.selectOne(querySwagger);
        return swagger;
    }

    public IPage<Swagger> getSwaggerPageByKind(Integer kind, String name,Integer status, Integer page, Integer limit) {
        //分页配置
        IPage<Swagger> swaggerIPage = new Page<>(page, limit);
        QueryWrapper<Swagger> queryWrapper = new QueryWrapper<>();
        IPage<Swagger> result;
        if(kind==0 && name=="" && status==9){
            result = swaggerMapper.selectPage(swaggerIPage, null);
        }else{
            if(kind!=0){
                queryWrapper.eq("kind", kind);
            }
            if(name!=""){
                queryWrapper.like("name", name);
            }
            if(status!=9){
                queryWrapper.eq("status", status);
            }
            result = swaggerMapper.selectPage(swaggerIPage, queryWrapper);
        }
        return result;
    }

    @Transactional
    public void updateSwaggerStatus(Integer id, Integer status, Long uid) {
        Swagger s1 = swaggerMapper.selectById(id);
        s1.setStatus(status);
        s1.setKind(s1.getKind());
        swaggerMapper.updateById(s1);
        if(status == -1){
            BlackList bl =new BlackList();
            BeanUtils.copyProperties(s1, bl);
            bl.setUid(uid);
            bl.setUpdateTime(TimeUtils.nowTime());
            blackListMapper.insert(bl);
        }else if(status == 0){
            QueryWrapper<BlackList> qb = new QueryWrapper<>();
            qb.eq("name", s1.getName()).eq("method", s1.getMethod());
            blackListMapper.delete(qb);
        }
    }

    public IPage<SwaggerLog> getSwaggerLogList(String name,Integer page, Integer limit) {
        IPage<SwaggerLog> swaggerLogIPage = new Page<>(page, limit);
        QueryWrapper<SwaggerLog> queryWrapper = new QueryWrapper<>();
        if(!name.isEmpty()){
            queryWrapper.like("name", name);
        }
        return swaggerLogMapper.selectPage(swaggerLogIPage, queryWrapper);
    }

    public Swagger getParamsByMethodAndName(String name, String method) {
        QueryWrapper<Swagger> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name).eq("method", method);
        return swaggerMapper.selectOne(queryWrapper);
    }
}
