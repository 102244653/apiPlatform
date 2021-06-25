package com.vantop.apitest.report;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vantop.apitest.mapper.ConfigKindMapper;
import com.vantop.apitest.mapper.DirMapper;
import com.vantop.apitest.mapper.ReportMapper;
import com.vantop.apitest.mapper.ResultMapper;
import com.vantop.apitest.report.model.Report;
import com.vantop.apitest.report.model.Result;
import com.vantop.apitest.vo.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class ReportService {

    @Autowired
    ReportMapper reportMapper;

    @Autowired
    ResultMapper resultMapper;

    @Autowired
    ConfigKindMapper configKindMapper;

    @Autowired
    DirMapper dirMapper;

    public void writeReport(String userName, Case runCase){
        Report Report = new Report();
        BeanUtils.copyProperties(runCase, Report);
        Report.setUrl(runCase.getGlobalEnv().getRemoteAddress()+runCase.getName());
        Report.setTestAccount(runCase.getTestUser().getAccount());
        Report.setTestUser(userName);
        if(runCase.getCaseType()==2){
            Report.setKindName(configKindMapper.selectById(runCase.getKindId()).getKindName());
        }else if(runCase.getCaseType()==1){
            Report.setKindName(configKindMapper.selectById(runCase.getKindDirId()/100).getKindName());
        }
        Report.setTestTime(new Date());
        reportMapper.insert(Report);
    }

    public IPage<Report> getReportList(String labelName, String tag, String taskId, Integer page, Integer limit) {
        IPage<Report> iPage = new Page<>(page, limit);
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("testTime");
        IPage<Report> result;
        if (labelName.isEmpty() && tag.isEmpty() && taskId.isEmpty()) {
            result = reportMapper.selectPage(iPage, queryWrapper);
        } else {
            if (!labelName.isEmpty()) {
                queryWrapper.eq("labelName", labelName.trim());
            }
            if (!tag.isEmpty()) {
                queryWrapper.like("tag", tag.trim());
            }
            if (!taskId.isEmpty()) {
                queryWrapper.like("taskId", taskId.trim());
            }
            result = reportMapper.selectPage(iPage, queryWrapper);
        }
        return result;
    }

    public IPage<Result> indexResult(Integer taskType, String taskId, String testUser, Integer page, Integer limit) {
        IPage<Result> iPage = new Page<>(page, limit);
        QueryWrapper<Result> queryWrapper = new QueryWrapper<>();
        if(!taskId.isEmpty()){
            queryWrapper.like("taskId", taskId);
        }
        if(!testUser.isEmpty()){
            queryWrapper.like("testUser", testUser);
        }
        queryWrapper.eq("taskType", taskType);
        queryWrapper.orderByDesc("id");
        return  resultMapper.selectPage(iPage, queryWrapper);
    }

    public IPage<Result> allResultDesc(String taskId, String testUser, Integer taskType ,Integer page, Integer limit) {
        IPage<Result> iPage = new Page<>(page, limit);
        QueryWrapper<Result> queryWrapper = new QueryWrapper<>();
        if(!taskId.isEmpty()){
            queryWrapper.like("taskId", taskId);
        }
        if(!testUser.isEmpty()){
            queryWrapper.like("testUser", testUser);
        }
        if(taskType!=9){
            queryWrapper.eq("tasktype", taskType);
        }

        queryWrapper.orderByDesc("testTime");
        return  resultMapper.selectPage(iPage, queryWrapper);
    }

    public Result getResultByTaskId(String taskId) {
        QueryWrapper<Result> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("taskId", taskId);
        return resultMapper.selectOne(queryWrapper);
    }

    public void saveResult(Result result, boolean isAdd) {
        try{
            if(isAdd){
                resultMapper.insert(result);
            }else {
                resultMapper.updateById(result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
