package com.vantop.apitest.single;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vantop.apitest.common.CaseService;
import com.vantop.apitest.report.ReportService;
import com.vantop.apitest.system.SystemService;
import com.vantop.apitest.utils.SqlExecute;
import com.vantop.apitest.mapper.*;
import com.vantop.apitest.single.model.SingleCase;
import com.vantop.apitest.single.model.SingleReport;
import com.vantop.apitest.swagger.model.Swagger;
import com.vantop.apitest.system.model.ConfigKind;
import com.vantop.apitest.system.model.DBConfig;
import com.vantop.apitest.system.model.Dir;
import com.vantop.apitest.system.model.GlobalEnv;
import com.vantop.apitest.user.model.TestAccount;
import com.vantop.apitest.user.model.User;
import com.vantop.apitest.vo.Case;
import com.vantop.apitest.vo.DebugVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SingleService {

    @Autowired
    SingleCaseMapper singleCaseMapper;

    @Autowired
    SingleReportMapper singleReportMapper;

    @Autowired
    DirMapper dirMapper;

    @Autowired
    ConfigKindMapper configKindMapper;

    @Autowired
    CaseService caseService;

    @Autowired
    TestAccountMapper testAccountMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    SwaggerMapper swaggerMapper;

    @Lazy
    @Autowired
    SystemService systemService;

    @Autowired
    ReportService reportService;


    public IPage<SingleCase> getSingleCaseList(int kindId,int kindDirId, int runStatus, Integer page, Integer limit) {
        IPage<SingleCase> iPage = new Page<>(page, limit);
        QueryWrapper<SingleCase> query = new QueryWrapper<>();
        IPage<SingleCase> result;
        if (kindId == 0 && kindDirId == 0 && runStatus == 100) {
            result = singleCaseMapper.selectPage(iPage, query);
        } else {
            if(kindDirId != 0) {
                query.eq("kindDirId", kindDirId);
            }else if(kindId!=0){
                query.between("kindDirId", kindId * 100, (kindId + 1) * 100);//大于
            }
            if(runStatus!=100){
                query.eq("runStatus", runStatus);
            }
            result = singleCaseMapper.selectPage(iPage, query);

        }
        return result;
    }

    public IPage<SingleReport> getSingleReportList(Long uid,String name, String tag, Integer page, Integer limit) {
        IPage<SingleReport> iPage = new Page<>(page, limit);
        QueryWrapper<SingleReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",uid);
        queryWrapper.orderByDesc("create_time");
        IPage<SingleReport> result;
        if (name.isEmpty() && tag.isEmpty()) {
            result = singleReportMapper.selectPage(iPage, queryWrapper);
        } else {
            if (!name.isEmpty()) {
                queryWrapper.eq("name", name.trim());
            }
            if (!tag.isEmpty()) {
                queryWrapper.like("tag", tag.trim());
            }
            result = singleReportMapper.selectPage(iPage, queryWrapper);
        }
        return result;
    }

    public IPage<Dir> getSingleDirList(int kindId, Integer page, Integer limit) {
        IPage<Dir> iPage = new Page<>(page, limit);
        IPage<Dir> result;
        if (kindId == 0) {
            result = dirMapper.selectPage(iPage, null);
        } else {
            QueryWrapper<Dir> query = new QueryWrapper<>();
            query.eq("kindId", kindId);
            result = dirMapper.selectPage(iPage, query);
        }
        return result;
    }

    public boolean addSingleCase(SingleCase singleCase) {
        try {
            singleCaseMapper.insert(singleCase);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addDir(Dir dir) {
        try {
            QueryWrapper<Dir> query0 = new QueryWrapper<>();
            query0.eq("kindId", dir.getKindId());
            query0.eq("dirName", dir.getDirName());
            Dir dbDir = dirMapper.selectOne(query0);
            if (dbDir != null) {
                return false;
            }

            QueryWrapper<Dir> query1 = new QueryWrapper<>();
            query1.eq("kindId", dir.getKindId());
            List<Dir> dirList = dirMapper.selectList(query1);
            int kindDirId = dir.getKindId() * 100 + dirList.size() + 1;

            QueryWrapper<ConfigKind> query2 = new QueryWrapper<>();
            query2.eq("kindId", dir.getKindId());
            ConfigKind configKind = configKindMapper.selectOne(query2);
            Dir creatDir = new Dir(dir.getKindId(), kindDirId, dir.getDirName(), configKind.getKindName());
            dirMapper.insert(creatDir);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSingleCase(Integer id) {
        try {
            singleCaseMapper.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSingleCase(Integer id, String field, String value) {
        try {
            SingleCase r = singleCaseMapper.selectById(id);
            setValue(r, r.getClass(), field, SingleCase.class.getDeclaredField(field).getType(), value);
            singleCaseMapper.updateById(r);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSingleCase2(SingleCase singleCase) {
        try {
            singleCaseMapper.updateById(singleCase);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void setValue(Object obj, Class<?> clazz, String filedName, Class<?> typeClass, Object value) {
        String methodName = "set" + filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
        try {
            Method method = clazz.getDeclaredMethod(methodName, new Class[]{typeClass});
            method.invoke(obj, new Object[]{value});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean updateSingleDir(Integer kindDirId, String field, String value) {
        try {
            QueryWrapper<Dir> query = new QueryWrapper<>();
            query.eq("kindDirId", kindDirId);
            Dir r = dirMapper.selectOne(query);
            setValue(r, r.getClass(), field, Dir.class.getDeclaredField(field).getType(), value);

            UpdateWrapper<Dir> update = new UpdateWrapper<>();
            update.eq("kindDirId", kindDirId);
            dirMapper.update(r, update);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public DebugVO runById(Long uid, Integer id) {
        log.info("Thead name:====>" + Thread.currentThread().getName());
        DebugVO debugVO = new DebugVO();

        QueryWrapper<User> queryUser = new QueryWrapper<>();
        queryUser.eq("uid", uid);
        User user = userMapper.selectOne(queryUser);

        QueryWrapper<TestAccount> queryAccount = new QueryWrapper<>();
        queryAccount.eq("uid", uid);
        queryAccount.eq("isDefault", 1);
        TestAccount defaultAccount = testAccountMapper.selectOne(queryAccount);

        if(defaultAccount == null){
            log.error("未设置默认测试账号");
            debugVO.setErrorLog("未设置默认测试账号");
            return debugVO;
        }

        GlobalEnv env= systemService.getEnvByUidAndPlatform(defaultAccount.getUid(), defaultAccount.getPlatform());
        DBConfig dbConfig = systemService.getDBConfigByPlatform(defaultAccount.getPlatform());
        if(dbConfig==null){
            debugVO.setErrorLog("请先配置测试数据库");
            return debugVO;
        }
        SqlExecute sqlExecute = new SqlExecute(dbConfig);

        QueryWrapper<SingleCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        SingleCase singleCase = singleCaseMapper.selectOne(queryWrapper);
        Case runCase = caseService.packageReqCase(env, singleCase);
        runCase = caseService.executeCase(runCase, defaultAccount, sqlExecute);
        sqlExecute.close();
//        writeLog(user, defaultAccount, env.getRemoteAddress(), singleCase, runCase);
        debugVO.setResult(runCase.getResult());
        debugVO.setAddress(env.getRemoteAddress());
        debugVO.setErrorLog(runCase.getErrorLog());
        debugVO.setResponse(runCase.getResponse());
        debugVO.setRequest(runCase.getParams());
        return debugVO;
    }

    public void writeLog(User user, TestAccount testAccount, String host, SingleCase reqInfo, Case ret) {
//        log.info("--------写日志------ret:"+ret);
        String url = host + ret.getName();
        SingleReport resultInfo = new SingleReport();
        resultInfo.setId(generalLogId(ret.getKindDirId()));
        resultInfo.setName(ret.getName());
        resultInfo.setUrl(url);
        resultInfo.setTag(ret.getTag());
        resultInfo.setReq(ret.getParams());
        resultInfo.setUid(user.getUid());
        resultInfo.setRunner(user.getUserName());
        resultInfo.setTest_account_id(testAccount.getId());
        resultInfo.setTest_account_name(testAccount.getAccount());
        resultInfo.setResult(ret.getErrorLog());
        resultInfo.setRes(ret.getResponse());
        resultInfo.setCreate_time(new Date());
        String taskId = System.currentTimeMillis()+"_"+reqInfo.getKindDirId();
        resultInfo.setTaskId(taskId);

        reqInfo.setStatus(2);//已执行

        if (ret.getResult().equals("fail")) {
            resultInfo.setSuccess(0);
            reqInfo.setRunStatus(2);//执行失败
        } else if (ret.getResult().equals("pass")) {
            resultInfo.setSuccess(1);
            reqInfo.setRunStatus(1);//执行成功
//            reqInfo.setLastRunner(mobile);
        }
        UpdateWrapper<SingleCase> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",reqInfo.getId());
        singleCaseMapper.update(reqInfo,updateWrapper);
        singleReportMapper.insert(resultInfo);
    }

    public String generalLogId(int kindDirId) {
        String logId = kindDirId + "" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(10);
        return logId;
    }

//    @Async("taskExecutor")
    public Boolean runAsync(String taskId, User user,TestAccount defaultAccount,SqlExecute sqlExecute,GlobalEnv env,SingleCase singleCase) {
        log.info("Thead name:====>" + Thread.currentThread().getName());
        Boolean flag = false;
        try {
            Case runCase = caseService.packageReqCase(env, singleCase);
            runCase.setTaskId(taskId);
            runCase = caseService.executeCase(runCase, defaultAccount, sqlExecute);
            if(runCase.getResult().equals("pass")){
                flag = true;
            }
            reportService.writeReport(user.getUserName(), runCase);
//            writeLog(user,defaultAccount,env.getRemoteAddress(),singleCase,runCase);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public boolean mockDir(Integer kindDirId) {
        try {
            QueryWrapper<SingleCase> query = new QueryWrapper<>();
            query.eq("kindDirId", kindDirId);
            query.eq("status", 1);
            List<SingleCase> list = singleCaseMapper.selectList(query);
            for(SingleCase r : list){
                String name = r.getName();
                int method =  r.getMethod();

                QueryWrapper<Swagger> querySwagger = new QueryWrapper<>();
                querySwagger.eq("name", name);
                querySwagger.eq("method", method2Str(method));
                Swagger swagger = swaggerMapper.selectOne(querySwagger);

                if(swagger != null) {
                    UpdateWrapper<SingleCase> update = new UpdateWrapper<>();
                    update.eq("name", name);
                    update.eq("method", method);
                    update.eq("status",1);
                    r.setParams(swagger.getParams());
                    singleCaseMapper.update(r, update);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String method2Str(int method){
        String str = "post";
        switch (method){
            case 0:
                str = "post";
                break;
            case 1:
                str = "get";
                break;
            case 2:
                str = "put";
                break;
            case 3:
                str = "delete";
                break;
        }
        return str;
    }

    public boolean copySingleCase(SingleCase singleCase) {
        try {
            singleCaseMapper.insert(singleCase);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
