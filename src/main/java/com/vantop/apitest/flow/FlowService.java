package com.vantop.apitest.flow;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vantop.apitest.common.CaseService;
import com.vantop.apitest.flow.model.FlowCase;
import com.vantop.apitest.flow.model.FlowLabel;
import com.vantop.apitest.mapper.*;
import com.vantop.apitest.report.ReportService;
import com.vantop.apitest.single.model.SingleCase;
import com.vantop.apitest.system.SystemService;
import com.vantop.apitest.system.model.DBConfig;
import com.vantop.apitest.system.model.GlobalEnv;
import com.vantop.apitest.user.model.TestAccount;
import com.vantop.apitest.utils.SqlExecute;
import com.vantop.apitest.vo.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Slf4j
@Transactional
public class FlowService {

    @Autowired
    FlowCaseMapper flowCaseMapper;

    @Autowired
    FlowLabelMapper flowLabelMapper;

    @Autowired
    TestAccountMapper testAccountMapper;

    @Autowired
    SwaggerMapper swaggerMapper;

    @Autowired
    ConfigKindMapper configKindMapper;

    @Lazy
    @Autowired
    CaseService caseService;

    @Autowired
    SingleCaseMapper singleCaseMapper;

    @Lazy
    @Autowired
    SystemService systemService;

    @Autowired
    ReportService reportService;

    public IPage<FlowLabel> getLabelList(Long uid, Integer kindId, String labelName, Integer page, Integer limit) {
        //分页配置
        IPage<FlowLabel> labelIPage = new Page<>(page, limit);
        QueryWrapper<FlowLabel> queryWrapper = new QueryWrapper<>();
        if(kindId != 0){
            queryWrapper.eq("kindId", kindId);
        }
        if(labelName != ""){
            queryWrapper.like("labelName", labelName);
        }
        //过滤掉管理员账号，显示全部
        if(!uid.toString().startsWith("11000000")){
            queryWrapper.and(i -> i.eq("userId", uid).or().eq("isOpen", 1));
        }
        IPage<FlowLabel> result = flowLabelMapper.selectPage(labelIPage, queryWrapper);
        return result;
    }

    public String addLabel(FlowLabel flowLabel) {
        QueryWrapper<FlowLabel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("kindId", flowLabel.getKindId())
                .eq("labelName", flowLabel.getLabelName())
                .eq("userId", flowLabel.getUserId());
        if(null == flowLabelMapper.selectOne(queryWrapper)){
            System.out.println("label:"+flowLabel);
            flowLabelMapper.insert(flowLabel);
            return "标签添加成功";
        }else {
            return "标签重复";
        }
    }

    public IPage<FlowCase> getCaseListByLabelId(Long uid, Integer labelId, Integer page, Integer limit) {
        ArrayList<Integer> labelIds = new ArrayList<>();
        IPage<FlowCase> caseIPage = new Page<>(page, limit);
        QueryWrapper<FlowCase> queryWrapper = new QueryWrapper<>();
        if(labelId == 0){
            for(FlowLabel flowLabel: this.getLabelByUid(uid)){
                labelIds.add(flowLabel.getId());
            }
        } else {
            labelIds.add(labelId);
        }
        Integer[] query = new Integer[labelIds.size()];
        queryWrapper.in("labelId", labelIds.toArray(query));
        IPage<FlowCase> result = flowCaseMapper.selectPage(caseIPage, queryWrapper);
        return result;
    }

    public List<FlowCase> getCaseListByLabelId(Integer labelId) {
        QueryWrapper<FlowCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("labelId", labelId)
                    .eq("status", 1)
                    .orderByAsc("executeId");
        List<FlowCase> result = flowCaseMapper.selectList(queryWrapper);
        return result;
    }

    public List<FlowLabel> getLabelByUid(Long uid) {
        QueryWrapper<FlowLabel> queryWrapper = new QueryWrapper<>();
        //过滤掉管理员账号，显示全部
        if(!uid.toString().startsWith("11000000")){
            queryWrapper.eq("userId", uid)
                    .or()
                    .eq("isOpen", 1);
        }
        List<FlowLabel> result = flowLabelMapper.selectList(queryWrapper);
        return result;
    }

    public List<Integer> getLabelListByUidAndStatus(Long uid) {
        QueryWrapper<FlowLabel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", uid)
                    .or()
                    .eq("isOpen", 1);
        List<FlowLabel> result = flowLabelMapper.selectList(queryWrapper);

        List<Integer> res = new ArrayList<>();
        for(FlowLabel flowLabel : result){
            if(flowLabel.getStatus()==1){
                res.add(flowLabel.getId());
            }
        }
        return res;
    }


    public String updateLabel(FlowLabel flowLabel) {

        try {
            flowLabelMapper.updateById(flowLabel);
            return "更新成功";
        }catch (Exception e){
            e.printStackTrace();
            return "编辑标签失败";
        }
    }

    public String addFlowCase(FlowCase flowCase) {
        if(flowCase.getDepend()==null || flowCase.getLabelId()==null ||
        flowCase.getAssertion()==null||flowCase.getParams()==null){
            return "请检查用例参数必填项！";
        }
        flowCase.setKindName(configKindMapper.selectById(flowCase.getKindId()).getKindName());
        flowCaseMapper.insert(flowCase);
        return "用例保存成功";
    }

    public String updateFlowCase(FlowCase flowCase) {
        if(flowCaseMapper.selectById(flowCase.getId())!= null){
            flowCase.setKindName(configKindMapper.selectById(flowCase.getKindId()).getKindName());
            flowCaseMapper.updateById(flowCase);
            return "用例保存成功!";
        }else {
            return "用例不存在!";
        }
    }

    public FlowLabel getLabelById(Integer labelId) {
        return flowLabelMapper.selectById(labelId);
    }

    public boolean importFlowCaseBySwagger(Integer labelId, Integer caseId, String labelName) {
        try {
            SingleCase singleCase = singleCaseMapper.selectById(caseId);
            FlowCase flowCase = new FlowCase();
            flowCase.setLabelId(labelId);
            flowCase.setLabelName(labelName);
            BeanUtils.copyProperties(singleCase, flowCase);
            flowCase.setKindId(Integer.valueOf(singleCase.getKindDirId() / 100));
            flowCase.setStatus(0);
            QueryWrapper<FlowCase> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("labelId", labelId);
            flowCase.setExecuteId(flowCaseMapper.selectCount(queryWrapper) + 1);
            flowCaseMapper.insert(flowCase);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public FlowCase getFlowCaseById(Integer id) {
        return flowCaseMapper.selectById(id);
    }

    public String deleteFlowCase(Integer id, Long uid) {
        FlowCase flowCase = flowCaseMapper.selectById(id);
        FlowLabel flowLabel = flowLabelMapper.selectById(flowCase.getLabelId());
        if(!uid.equals(flowLabel.getUserId())){
            return "只允许编辑自己的用例标签！";
        }
        flowCaseMapper.deleteById(id);
        return "用例删除成功！";
    }

    public HashMap<Integer, Object> runSingleFlowLabel(String userName, String taskId, Integer labelId) {
        HashMap<Integer, Object> res = new HashMap<>();

        FlowLabel flowLabel = this.getLabelById(labelId);
        if(flowLabel.getAccountId()==null){
            res.put(labelId,"请在场景标签中设置测试账号！");
            return res;
        }
        if(flowLabel.getStatus()==0){
            res.put(labelId,"请修改标签状态为启用！");
            return res;
        }
        TestAccount testAccount = testAccountMapper.selectById(flowLabel.getAccountId());
        if(testAccount==null){
            res.put(labelId,"场景标签中维护的测试账号不存在，请重置！");
            return res;
        }
        GlobalEnv env = systemService.getEnvByUidAndPlatform(flowLabel.getUserId(), testAccount.getPlatform());
        DBConfig dbConfig = systemService.getDBConfigByPlatform(env.getPlatForm());
        if(dbConfig==null){
            res.put(labelId,"请先配置测试数据库！");
            return res;
        }

        List<FlowCase> flowCaseList = this.getCaseListByLabelId(labelId);
        if(flowCaseList.size()==0){
            res.put(labelId,"无可执行的用例！");
            return res;
        }

        SqlExecute sqlExecute = new SqlExecute(dbConfig);

        List<Case> resultCase = new ArrayList<>();
        for(FlowCase flowCase: flowCaseList){
            Case runCase = caseService.packageFlowCase(env, flowCase);
            runCase.setTaskId(taskId);
            runCase = caseService.executeCase(runCase, testAccount, sqlExecute);
            reportService.writeReport(userName, runCase);
            resultCase.add(runCase);
        }
        sqlExecute.close();
        res.put(labelId, resultCase);
        return res;
    }


}

