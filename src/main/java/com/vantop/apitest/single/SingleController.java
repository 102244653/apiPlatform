package com.vantop.apitest.single;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vantop.apitest.mapper.SingleCaseMapper;
import com.vantop.apitest.mapper.TestAccountMapper;
import com.vantop.apitest.mapper.UserMapper;
import com.vantop.apitest.report.ReportService;
import com.vantop.apitest.report.model.Report;
import com.vantop.apitest.report.model.Result;
import com.vantop.apitest.single.model.SingleCase;
import com.vantop.apitest.single.model.SingleReport;
import com.vantop.apitest.system.SystemService;
import com.vantop.apitest.system.model.DBConfig;
import com.vantop.apitest.system.model.Dir;
import com.vantop.apitest.system.model.GlobalEnv;
import com.vantop.apitest.user.model.TestAccount;
import com.vantop.apitest.user.model.User;
import com.vantop.apitest.utils.SqlExecute;
import com.vantop.apitest.utils.TimeUtils;
import com.vantop.apitest.vo.CurrentUserVO;
import com.vantop.apitest.vo.DebugVO;
import com.vantop.apitest.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/va")
public class SingleController {

    @Autowired
    SingleService singleService;

    @Autowired
    SingleCaseMapper singleCaseMapper;

    @Autowired
    TestAccountMapper testAccountMapper;

    @Autowired
    UserMapper userMapper;

    @Lazy
    @Autowired
    SystemService systemService;

    @Autowired
    ReportService reportService;

    /**
     * 接口列表
     * */
    @GetMapping("/singleCaseList")
    public JSON singleCaseList(@RequestParam("kind") Integer kind,@RequestParam("kind2") Integer kind2,@RequestParam("runStatus") Integer runStatus, @RequestParam("page") Integer page, @RequestParam("limit") Integer limit) {
//        log.info("=============kind:"+kind+",kind2:"+kind2);
        IPage<SingleCase> result = singleService.getSingleCaseList(kind, kind2, runStatus, page, limit);
        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    /**
     * 日志列表
     * */
    @GetMapping("/singleReportList")
    public JSON singleReportList(@RequestParam("uid") Long uid,@RequestParam("page") Integer page, @RequestParam("limit") Integer limit,@RequestParam("name") String name,
                                 @RequestParam("tag") String tag) {
        IPage<SingleReport> result = singleService.getSingleReportList(uid,name,tag,page, limit);
        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    /**
     * 目录列表
     * */
    @GetMapping("/singleDirList")
    public JSON singleDirList(@RequestParam("kind") Integer kind, @RequestParam("page") Integer page, @RequestParam("limit") Integer limit) {
        IPage<Dir> result = singleService.getSingleDirList(kind, page, limit);
        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    /**
     * 添加接口
     * */
    @PostMapping("/addSingleCase")
    public JSON addSingleCase(@RequestBody SingleCase singleCase){
        if(singleCase.getKindDirId()==0){
            return ResultVO.failure("请选择模块和分类信息！");
        }
        if(singleService.addSingleCase(singleCase)) {
            return ResultVO.success("添加成功！");
        }else{
            return ResultVO.failure("添加失败！");
        }
    }

    /**
     * 修改接口
     * */
    @PostMapping("/updateSingleCase2")
    public JSON updateSingleCase2(@RequestBody SingleCase singleCase){
//        log.info("======updateSingleCase2====>"+singleCase);
        if(singleService.updateSingleCase2(singleCase)) {
            return ResultVO.success("修改成功！");
        }else{
            return ResultVO.success("修改失败！");
        }
    }

    /**
     * 添加目录
     * */
    @PostMapping("/addDir")
    public JSON addDir(@RequestBody Dir dir){
        if(singleService.addDir(dir)) {
            return ResultVO.success("添加成功！");
        }else{
            return ResultVO.failure("添加失败！");
        }
    }

    /**
     * 删除用例
     * */
    @PostMapping("/deleteSingleCase")
    public JSON deleteSingleCase(@RequestParam("id") Integer id){
        try {
            if(singleService.deleteSingleCase(id)) {
                return ResultVO.success();
            }else{
                return ResultVO.failure();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    /**
     * 执行用例
     * */
    @PostMapping("/runSingleCase")
    public JSON runSingleCase(@RequestParam("uid") Long uid,@RequestParam("id") Integer id,HttpSession session){
//        log.info("====执行用例===uid:"+uid+",id:"+id);
        DebugVO ret = singleService.runById(uid,id);
        return ResultVO.success(ret);
    }

    /**
     * 按目录批量
     * */
    @PostMapping("/runBatch")
    public JSON runBatch(@RequestParam("uid") Long uid, @RequestParam("kindDirId") Integer kindDirId){
        log.info("====批量执行===uid:"+uid+",kindDirId:"+kindDirId);
        List<SingleCase> list = new ArrayList<>();
        QueryWrapper<SingleCase> query = new QueryWrapper<>();
        query.eq("kindDirId", kindDirId);
        list = singleCaseMapper.selectList(query);
        if(list.size()==0){
            return ResultVO.failure("模块未找到可执行用例！");
        }

        QueryWrapper<User> queryUser = new QueryWrapper<>();
        queryUser.eq("uid", uid);
        User user = userMapper.selectOne(queryUser);

        QueryWrapper<TestAccount> queryAccount = new QueryWrapper<>();
        queryAccount.eq("uid", uid);
        queryAccount.eq("isDefault", 1);
        TestAccount defaultAccount = testAccountMapper.selectOne(queryAccount);

        GlobalEnv env= systemService.getEnvByUidAndPlatform(defaultAccount.getUid(), defaultAccount.getPlatform());

        //初始化sql执行器
        DBConfig dbConfig = systemService.getDBConfigByPlatform(defaultAccount.getPlatform());
        if(dbConfig==null){
            return ResultVO.failure("未设置测试环境数据库配置信息！");
        }
        SqlExecute sqlExecute = new SqlExecute(dbConfig);

        String taskId = "Single " + TimeUtils.getTime();
        Result singleResult = new Result();
        singleResult.setTaskId(taskId);
        singleResult.setRunStatus(0);//执行开始
        singleResult.setTestUser(user.getUserName());
        singleResult.setTestTime(TimeUtils.nowTime());
        singleResult.setTaskType(1);
        reportService.saveResult(singleResult, true);

        int success = 0;
        for (SingleCase singleCase : list) {
            if(singleService.runAsync(taskId, user,defaultAccount,sqlExecute,env, singleCase)){
                success++;
            }
        }
        sqlExecute.close();
        Result singleResult1 = reportService.getResultByTaskId(taskId);
        singleResult1.setTotalCase(list.size());
        singleResult1.setCasePass(success);
        singleResult1.setCaseFail(list.size()-success);
        singleResult1.setRunStatus(1);//执行结束
        singleResult1.setLogs(list.size()-success == 0 ? "执行成功":"请查看详情");
        reportService.saveResult(singleResult1, false);

        return ResultVO.success("执行用例总数:"+list.size()+",测试通过:"+success);
    }


    @PostMapping("/updateSingleCase")
    public JSON updateSingleCase(@RequestParam("id") Integer id,@RequestParam("field") String field,@RequestParam("value") String value,HttpSession session){
        try {
            singleService.updateSingleCase(id,field,value);
            return ResultVO.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    @PostMapping("/updateSingleDir")
    public JSON updateSingleDir(@RequestParam("kindDirId") Integer kindDirId,@RequestParam("field") String field,@RequestParam("value") String value,HttpSession session){
        try {
            singleService.updateSingleDir(kindDirId,field,value);
            return ResultVO.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    /**
     * mock分类
     * */
    @PostMapping("/mockDir")
    public JSON mockDir(@RequestParam("kindDirId") Integer kindDirId){
        try {
            if(kindDirId==0){
                return ResultVO.failure("请选择分类！");
            }
            log.info("=====mock=====kindDirId:"+kindDirId);
            if(singleService.mockDir(kindDirId)) {
                return ResultVO.success("mock成功");
            }else{
                return ResultVO.failure("mock失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    /**
     * 复制用例
     * */
    @PostMapping("/copySingleCase")
    public JSON copySingleCase(@RequestBody SingleCase singleCase){
        log.info("======copySingleCase====>"+singleCase);
        if(singleService.copySingleCase(singleCase)) {
            return ResultVO.success("复制成功！");
        }else{
            return ResultVO.success("复制失败！");
        }
    }

}
