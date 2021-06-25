package com.vantop.apitest.report;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vantop.apitest.report.model.Report;
import com.vantop.apitest.report.model.Result;
import com.vantop.apitest.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/va")
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("/reportList")
    public JSON Report(@RequestParam("page") Integer page, @RequestParam("limit") Integer limit, @RequestParam("labelName") String labelName,
                           @RequestParam("tag") String tag, @RequestParam("taskId") String taskId) {
        IPage<Report> result = reportService.getReportList(labelName, tag, taskId, page, limit);
        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    @GetMapping("/resultList")
    public JSON Result(@RequestParam("taskId") String taskId, @RequestParam("testUser") String testUser, @RequestParam("taskType") Integer taskType,
                           @RequestParam("page") Integer page, @RequestParam("limit") Integer limit){
        IPage<Result> Results = reportService.allResultDesc(taskId, testUser, Integer.valueOf(taskType), page, limit);
        return ResultVO.success(Results.getTotal(),Results.getRecords());
    }

    @GetMapping("/indexFlowResult")
    public JSON indexResult(@RequestParam("taskId") String taskId, @RequestParam("testUser") String testUser,
                            @RequestParam("page") Integer page, @RequestParam("limit") Integer limit){
        IPage<Result> Results = reportService.indexResult(2, taskId, testUser, page, limit);
        List<Result> res = Results.getRecords() ;
        Collections.reverse(res);//list倒序排列
        return ResultVO.success(Results.getTotal(), res);
    }


}
