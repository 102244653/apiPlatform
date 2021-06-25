package com.vantop.apitest.common;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.vantop.apitest.flow.FlowService;
import com.vantop.apitest.flow.model.FlowCase;
import com.vantop.apitest.mapper.FlowCaseMapper;
import com.vantop.apitest.mapper.TestAccountMapper;
import com.vantop.apitest.single.model.SingleCase;
import com.vantop.apitest.system.model.GlobalEnv;
import com.vantop.apitest.user.model.TestAccount;
import com.vantop.apitest.utils.SqlExecute;
import com.vantop.apitest.utils.http.HttpConfig;
import com.vantop.apitest.utils.http.HttpUtil;
import com.vantop.apitest.vo.Case;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
@Service
public class CaseService {

    @Lazy
    @Autowired
    FlowService flowService;

    @Autowired
    TestAccountMapper testAccountMapper;

    @Autowired
    FlowCaseMapper flowCaseMapper;

    public Case packageReqCase(GlobalEnv globalEnv, SingleCase reqInfo) {
        Case reqCase = new Case();
        reqCase.setId(reqInfo.getId());
        reqCase.setKindDirId(reqInfo.getKindDirId());
        reqCase.setCaseType(1);//单接口用例类型1
        reqCase.setTag(reqInfo.getTag());
        reqCase.setGlobalEnv(globalEnv);
        reqCase.setName(reqInfo.getName());
        reqCase.setMethod(reqInfo.getMethod());
        reqCase.setParams(reqInfo.getParams());
        reqCase.setBefore(reqInfo.before());
        reqCase.setAssertion(reqInfo.assertion());
        reqCase.setAfter(reqInfo.after());
        return reqCase;
    }

    public Case packageFlowCase(GlobalEnv globalEnv, FlowCase flow) {
        Case flowCase = new Case();
        flowCase.setId(flow.getId());
        flowCase.setKindId(flow.getKindId());
        flowCase.setCaseType(2);//流程接口用例类型2
        flowCase.setTag(flow.getTag());
        flowCase.setExecuteId(flow.getExecuteId());
        flowCase.setLabelId(flow.getLabelId());
        flowCase.setLabelName(flow.getLabelName());
        flowCase.setGlobalEnv(globalEnv);
        flowCase.setName(flow.getName());
        flowCase.setMethod(flow.getMethod());
        flowCase.setParams(flow.getParams());
        flowCase.setBefore(flow.before());
        flowCase.setAssertion(flow.assertion());
        flowCase.setAfter(flow.after());
        return flowCase;
    }

    public Case executeCase(Case vanTopCase, TestAccount testAccount, SqlExecute sqlExecute){
        try {
            vanTopCase = this.beforeCase(vanTopCase, sqlExecute);
            if (!this.checkAccountStatus(vanTopCase.getGlobalEnv(), testAccount)) {
                this.loginSaas(vanTopCase.getGlobalEnv(), testAccount);
                //取数据库更新后的测试信息
                testAccount = testAccountMapper.selectById(testAccount.getId());
            }
            vanTopCase.setTestUser(testAccount);
            vanTopCase = this.VanTopHttp(vanTopCase);
            vanTopCase = this.assertCase(vanTopCase, sqlExecute);
        }catch (Exception e){
            e.printStackTrace();
            vanTopCase.setErrorLog(vanTopCase.getErrorLog()+"\n"+e.getMessage());
            vanTopCase.setResult("fail");
        }
        try{
            vanTopCase = this.afterCase(vanTopCase, sqlExecute);
        }catch (Exception e){
            e.printStackTrace();
        }
        return vanTopCase;
    }

    public Case VanTopHttp(Case testCase){
        String response = null;
        String url = testCase.getGlobalEnv().getRemoteAddress() + testCase.getName();
        switch (testCase.getMethod()){
            case 0:
                response = HttpUtil.PostJson(url, testCase.getParams(), testCase.getGlobalEnv().getUrlPre(), testCase.getTestUser().getLoginDetail(), testCase.getTestUser().getCookie());
                break;
            case 1:
                url += testCase.getParams();
                response = HttpUtil.GetURL(url, testCase.getGlobalEnv().getUrlPre(), testCase.getTestUser().getLoginDetail(),  testCase.getTestUser().getCookie());
                break;
            case 2:
                response = HttpUtil.PutJson(url, testCase.getParams(), testCase.getGlobalEnv().getUrlPre(), testCase.getTestUser().getLoginDetail(), testCase.getTestUser().getCookie());
                break;
            case 3:
                url += testCase.getParams();
                response = HttpUtil.senddelete(url, testCase.getGlobalEnv().getUrlPre(), testCase.getTestUser().getLoginDetail(),  testCase.getTestUser().getCookie());
                break;
            default:
                log.error("暂不支持该类型请求：" + testCase.getMethod());

        }
        if (response != null) {
            testCase.setResponse(response);
        }else {
            testCase.setResponse("{\"test\":\"request fail\"}");
        }
        if(testCase.getCaseType().equals(2)){
            FlowCase res = flowCaseMapper.selectById(testCase.getId());
            res.setResponse(testCase.getResponse());
            flowCaseMapper.updateById(res);
        }
        return testCase;
    }

    public Case beforeCase(Case singleCase, SqlExecute sqlExecute){
        /**
         * 前置操作的处理
         * */
        HashMap<String, String> before = singleCase.getBefore();
        for (HashMap.Entry<String, String> entry : before.entrySet()) {
            String value = entry.getValue();
            switch (entry.getKey()) {
                case "depend":
                    singleCase = this.beforeDepend(singleCase, value);
                    break;
                case "randKey":
                    singleCase = this.beforeRandKey(singleCase, value);
                    break;
                case "beforeSql":
                    singleCase = this.beforeSql(singleCase, value, sqlExecute);
                    break;
                default:
                    log.error("不支持该类型的操作：" + entry.getKey());
            }
        }
        return singleCase;
    }

    public Case assertCase(Case singleCase, SqlExecute sqlExecute){
        /**
         * 断言操作的处理
         * */
        HashMap<String, String> result = singleCase.getAssertion();
        int fail = 0;//断言失败的次数
        String errorLog = singleCase.getErrorLog();
        if(JSONObject.parseObject(singleCase.getResponse()).containsKey("test")){
            singleCase.setResult("fail");
            singleCase.setErrorLog(errorLog+"接口请求失败！\n");
            return singleCase;
        }
        HashMap<String, String> assertCase = singleCase.getAssertion();
        for (HashMap.Entry<String, String> entry : assertCase.entrySet()) {
            String value = entry.getValue();
            switch (entry.getKey()) {
                case "assertion":
                    HashMap<String, Object> ar = this.assertResponse(singleCase, value);
                    fail = fail + (int) (ar.get("fail"));//统计失败次数
                    errorLog += ar.get("error");
                    break;
                case "afterDBCheck":
                    HashMap<String, Object> ac = this.afterDBCheck(singleCase, value, sqlExecute);
                    fail = fail + (int) (ac.get("fail"));//统计失败次数
                    errorLog += ac.get("error");
                    break;
                default:
                    errorLog += "assertCase不支持该类型的操作：" + entry.getKey()+"\n";
            }
        }
        singleCase.setErrorLog(errorLog);
        if(fail!=0){
            singleCase.setResult("fail");
        }else {
            singleCase.setResult("pass");
        }
        return singleCase;
    }

    public Case afterCase(Case singleCase, SqlExecute sqlExecute){
        String errorLog = singleCase.getErrorLog();
        HashMap<String, String> after = singleCase.getAfter();
        for (HashMap.Entry<String, String> entry : after.entrySet()) {
            String value = entry.getValue();
            switch (entry.getKey()) {
                case "afterSql":
                    errorLog += this.afterSql(value, sqlExecute);
                    break;
                default:
                    errorLog += "afterCase不支持该类型的操作：" + entry.getKey()+"\n";
            }
        }
        singleCase.setErrorLog(errorLog);
        return singleCase;

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    public Case beforeDepend(Case singleCase, String depend) {
        String oldParams = singleCase.getParams().trim();
        try{
            JSONArray depends = JSONObject.parseObject(depend).getJSONArray("depends");
            if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){//get和delete请求参数为string
                if(singleCase.getParams().startsWith("[") && singleCase.getParams().endsWith("]")){
                    oldParams = JSONArray.parseArray(oldParams).toJSONString();
                }else {
                    oldParams = JSONObject.parseObject(oldParams).toJSONString();
                }
            }
            for (int i = 0; i < depends.size(); i++) {
                JSONObject dep = (JSONObject) depends.get(i);
                int caseId = dep.getInteger("caseId");//以来的用例id
                String resPath = dep.getString("keyValue");//取值的那个path
                String paraPath = dep.getString("path");//替换的那个path
                String valueType = "";
                if(dep.containsKey("valueType")){
                    valueType = dep.getString("valueType").trim();
                }
                FlowCase flowCase = flowCaseMapper.selectById(caseId);
                log.info("====依赖用例ID:" + caseId + ",依赖字段:" + paraPath+"=====");

                if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){
                    Object res = JsonPath.read(JSONObject.parseObject(flowCase.getResponse()), resPath);
                    oldParams = JsonPath.parse(oldParams).set(paraPath, this.changeType(res, valueType)).jsonString();
                }else {
                    oldParams = oldParams.replace(paraPath, JsonPath.read(JSONObject.parseObject(flowCase.getResponse()), resPath));
                }
            }
            singleCase.setParams(oldParams);
            log.info("newParams:"+singleCase.getParams());
        }catch (Exception e){
            e.printStackTrace();
            singleCase.setErrorLog(singleCase.getErrorLog()+"beforeDepend处理失败；\n");
        }
        return singleCase;
    }

    public Case beforeRandKey(Case singleCase, String randKey) {
        String oldParams = singleCase.getParams().trim();
        try {
            JSONArray randKeys = JSONObject.parseObject(randKey).getJSONArray("randKeys");
            if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){//get和delete请求参数为string
                if(singleCase.getParams().startsWith("[") && singleCase.getParams().endsWith("]")){
                    oldParams = JSONArray.parseArray(oldParams).toJSONString();
                }else {
                    oldParams = JSONObject.parseObject(oldParams).toJSONString();
                }
            }
            for (int i = 0; i < randKeys.size(); i++) {
                JSONObject rand = (JSONObject) randKeys.get(i);
                String paraPath =rand.getString("path").trim();
                String title="";
                if(rand.containsKey("title")){
                    title = rand.getString("title").trim();
                }
                int randType = rand.getInteger("type");
                int randNum = rand.getInteger("num");
                log.info("====key:" + paraPath + ",randType:" + randType + ",randNum:" + randNum);
                switch (randType) {
                    case 1://数字
                        if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){
                            oldParams = JsonPath.parse(oldParams).set(paraPath, RandomStringUtils.randomNumeric(randNum)).jsonString();
                        }else {
                            oldParams = oldParams.replace(paraPath, RandomStringUtils.randomNumeric(randNum));
                        }
                        break;
                    case 2://字符串
                        if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){
                            String text = title+RandomStringUtils.randomAlphanumeric(randNum);
                            oldParams = JsonPath.parse(oldParams).set(paraPath, text).jsonString();
                        }else {
                            oldParams = oldParams.replace(paraPath, title+RandomStringUtils.randomAlphanumeric(randNum));
                        }
                        break;
                    case 3://时间戳
                        if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){
                            oldParams = JsonPath.parse(oldParams).set(paraPath, String.valueOf(System.currentTimeMillis())).jsonString();
                        }else {
                            oldParams = oldParams.replace(paraPath, String.valueOf(System.currentTimeMillis()));
                        }
                        break;
                }

            }
            singleCase.setParams(oldParams);
            log.info("newParams:" + singleCase.getParams());
        }catch (Exception e){
            e.printStackTrace();
            singleCase.setErrorLog(singleCase.getErrorLog()+"beforeRandKey处理失败；\n");
        }
        return singleCase;
    }


    public Case beforeSql(Case singleCase, String sql, SqlExecute sqlExecute) {
        String oldParams = singleCase.getParams().trim();
        try {
            JSONObject bsql = JSONObject.parseObject(sql);
            //get和delete请求参数为string
            if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){
                if(singleCase.getParams().startsWith("[") && singleCase.getParams().endsWith("]")){
                    oldParams = JSONArray.parseArray(oldParams).toJSONString();
                }else {
                    oldParams = JSONObject.parseObject(oldParams).toJSONString();
                }
            }
            String mysql = bsql.getString("sql").trim();
            HashMap<String, Object> SQLMap = sqlExecute.runSql(mysql);
            JSONArray paths = bsql.getJSONArray("paths");
            if(SQLMap.get("sqlResult").equals("pass") && SQLMap.size()>1 && paths.size()!=0){
                for (int k = 0; k < paths.size(); k++) {
                    JSONObject p = (JSONObject) paths.get(k);
                    String paraPath = p.getString("path").trim();
                    String key = p.getString("keyName").trim();
                    String valueType = "";
                    if(p.containsKey("valueType")){
                        valueType = p.getString("valueType").trim();
                    }
                    if (!paraPath.isEmpty() && !key.isEmpty()) {
                        if(singleCase.getMethod() == 0 || singleCase.getMethod()==2){
                            oldParams = JsonPath.parse(oldParams).set(paraPath, this.changeType(SQLMap.get(key), valueType)).jsonString();
                        }else {
                            oldParams = oldParams.replace(paraPath, String.valueOf(SQLMap.get(key)));
                        }
                    }
                }
            }else if(SQLMap.get("sqlResult").equals("fail")){
                singleCase.setErrorLog(singleCase.getErrorLog()+"beforeSql执行失败；\n");
            }
            singleCase.setParams(JSON.parseObject(oldParams).toJSONString());
            log.info("newParams:" + singleCase.getParams());
        }catch (Exception e){
            e.printStackTrace();
            singleCase.setErrorLog(singleCase.getErrorLog()+"beforeSql处理失败；\n");
        }
        return singleCase;
    }

    public String afterSql(String sql,SqlExecute sqlExecute) {
        String error = "";
        //清除数据库数据
        JSONArray sqls = JSONObject.parseObject(sql).getJSONArray("sqls");
        for (int i = 0; i < sqls.size(); i++) {
            String mysql = (String) sqls.get(i);
            HashMap<String, Object> res = sqlExecute.runSql(mysql.trim());
            if(res.get("sqlResult").equals("fail")){
                error += mysql+"执行失败;"+"\n";
            }
        }
        return error;
    }

    public HashMap<String, Object> afterDBCheck(Case singleCase, String check,SqlExecute sqlExecute) {
        HashMap<String, Object> checkResult = new HashMap<>();
        int fail = 0;
        String error = "";
        JSONObject checkJson = JSONObject.parseObject(check);
        String sql = checkJson.getString("sql").trim();
        HashMap<String, Object> SQLMap = sqlExecute.runSql(sql);
        //sql结果不为空则进行断言
        if(SQLMap.get("sqlResult").equals("pass") && SQLMap.size()>1){
            JSONArray checks = checkJson.getJSONArray("paths");
            JSONObject response = JSONObject.parseObject(singleCase.getResponse());
            for(int i = 0; i < checks.size(); i++){
                JSONObject s = (JSONObject) checks.get(i);
                String paraPath = s.getString("path").trim();
                String key = s.getString("keyValue").trim();
                String type = s.getString("checkType");
                //断言格式正确
                if(!paraPath.isEmpty() && !key.isEmpty()){
                    String value = String.valueOf(SQLMap.get(key));
                    String real = JsonPath.read(response, paraPath);
                    log.info("afterDBCheck：比较方式="+type+",预期="+value+",实际="+real);
                    //断言方法：记录失败次数和失败的内容
                    if(!this.resultAssert(type, real, value)){
                        fail += 1;
                        error +="afterDBCheck断言失败:比较方式="+type+",预期="+value+",实际="+real+";\n";
                    }
                }else {
                    fail += 1;
                    error += "断言格式错误："+s.toJSONString();
                }
            }
        }else {
            fail = 999;
            error = sql+"执行结果为空，断言失败;";
        }

        checkResult.put("fail", fail);
        checkResult.put("error", error);
        return checkResult;
    }

    public HashMap<String, Object> assertResponse(Case singleCase, String assertion) {
        HashMap<String, Object> checkResult = new HashMap<>();
        int fail = 0;
        String error = "";
        JSONArray checks = JSONObject.parseObject(assertion).getJSONArray("assertion");
        JSONObject response = JSONObject.parseObject(singleCase.getResponse());
        for(int i = 0; i < checks.size(); i++){
            JSONObject ass = (JSONObject) checks.get(i);

            String value = ass.getString("keyValue").trim();
            String type = ass.getString("checkType");
            String paraPath = ass.getString("path");
            // 断言参数格式正确
            if(!paraPath.isEmpty() && !value.isEmpty()){
                String real = JsonPath.read(response, paraPath);
                log.info("assertion：比较方式="+type+",预期="+value+",实际="+real);
                //断言方法：记录失败次数和失败的内容
                if(!this.resultAssert(type, real, value)){
                    fail += 1;
                    error +="assertResponse断言失败:比较方式="+type+",预期="+value+",实际="+real+";\n";
                }
            }else {
                fail += 1;
                error += "断言格式错误："+ass.toJSONString();

            }
        }
        checkResult.put("fail", fail);
        checkResult.put("error", error);
        return checkResult;
    }


    public boolean resultAssert(String type, String real, String expect){
        boolean flag = false;
        switch (type){
            case "equals":
                if(real.equals(expect)){
                    flag = true;
                }
                break;
            case "start":
                if(real.startsWith(expect)){
                    flag = true;
                }
                break;
            case "end":
                if(real.endsWith(expect)){
                    flag = true;
                }
                break;
            case "contains":
                if(real.contains(expect)){
                    flag = true;
                }
                break;
            default:
                log.error("暂不支持该类型的断言："+type);
                break;
        }
        return flag;
    }

    public Object changeType(Object value, String type){
        if(type==""){
            return value;
        }
        String real = this.getType(value).split(".")[-1];
        Object re = null;
        try{
            if(!real.contains("string") && type.equals("string")){
                re = String.valueOf(value);
            }else if(!real.contains("int") && type.equals("int")){
                re = Integer.parseInt((String) value);
            }else if(!real.contains("long") && type.equals("long")){
                re = Long.valueOf((String) value);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("数据类型转换错误");
        }
        return re;
    }

    public String getType(Object obj){
        return obj.getClass().getName().toLowerCase();
    }

    public boolean loginSaas(GlobalEnv globalEnv, TestAccount testAccount) {
        Boolean isLogin = false;
        String params = "{\"account\":\""+testAccount.getAccount()+"\",\"password\":\""+testAccount.getPassword()+"\",\"captcha\":\"0000\"}";
        String url = globalEnv.getRemoteAddress() + "/auth/login";
        CloseableHttpResponse response = HttpUtil.PostLogin(url, params,"api.nextop.cc");
        // 得到响应实例
        org.apache.http.HttpEntity entity = response.getEntity();
        //可以获得响应头
        Header[] headers = response.getHeaders("Set-Cookie");
        String cookie = null;
        for (Header header : headers) {
            if(header.getValue().startsWith("SESSION")){
               cookie = header.getValue().split(";")[0];
            }
        }
        log.info("COOKIE::"+cookie);
        // 判断响应状态
        String responseContent = "";
        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
            try {
                responseContent = EntityUtils.toString(entity, HttpConfig.CHARSET_UTF_8);
                log.info("[Response]:" + responseContent);
                EntityUtils.consume(entity);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            // 释放资源
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject loginDetail = JSONObject.parseObject(responseContent);
        String token = null;
        if(loginDetail.getString("code").equals("000000")){
            token = loginDetail.getJSONObject("data").getJSONObject("tokenInfo").getString("token");
            isLogin = true;
        }
        //更新数据库测试账号信息
        testAccount.setLoginDetail("bearer "+token);
        testAccount.setCookie(cookie);
        testAccountMapper.updateById(testAccount);
        return isLogin;
    }


    public boolean checkAccountStatus(GlobalEnv globalEnv, TestAccount testAccount){
        if(testAccount.getLoginDetail()==null){
            return false;
        }
        //获取更新信息接口
        String url = globalEnv.getRemoteAddress() + "/user/user/userInfo";
        String body = HttpUtil.GetURL(url, globalEnv.getUrlPre(), testAccount.getLoginDetail(), testAccount.getCookie());
        if(JSONObject.parseObject(body).getString("code").equals("000000") && JSONObject.parseObject(body).getJSONObject("data").
                getJSONObject("userInfo").getString("account").
                equals(testAccount.getAccount())){
            return true;
        }else {
            return false;
        }

    }


//    public String loginInternet(Case testCase) {
//        TestAccount testAccount = testCase.getTestUser();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("x-ca-reqid", String.valueOf(System.currentTimeMillis()));
//        headers.set("x-ca-reqtime", String.valueOf(System.currentTimeMillis()));
//        String params = "";
//        params = JsonPath.parse(params).set("$.email", testAccount.getAccount()).jsonString();
//        params = JsonPath.parse(params).set("$.password", testAccount.getPassword()).jsonString();
//        HttpEntity<String> entity = new HttpEntity<String>(params, headers);
//        ResponseEntity<String> responseEntity = null;
//        String url = testCase.getHost() + "/api/user-service/auth/login";
//        responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//        String body = responseEntity.getBody();
//    }

//    public static void main(String[] args) {
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("x-ca-reqid", String.valueOf(System.currentTimeMillis()));
//        headers.set("x-ca-reqtime", String.valueOf(System.currentTimeMillis()));
//        headers.set("Host", "api.nextop.cc");
//        String params = "{\"account\":\"13534173939\",\"password\":\"Td147852\",\"captcha\":\"0000\"}";
//        HttpEntity<String> entity = new HttpEntity<String>(params, headers);
//        ResponseEntity<String> responseEntity = null;
//        String url = "http://172.24.14.70:80/auth/login";
//        responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//        JSONObject body = JSONObject.parseObject(responseEntity.getBody());
//        String token= "";
//        String cookies = "";
//        if(body.getString("code").equals("000000")){
//            token = body.getJSONObject("data").getJSONObject("tokenInfo").getString("token");
//            List<String> cookieList = responseEntity.getHeaders().get("Set-Cookie");
//            for(String cook:cookieList){
//                cookies += cook;
//                log.info(cook);
//            }
//            log.info(cookies);
//            log.info(token);
//        }
//    }

}




