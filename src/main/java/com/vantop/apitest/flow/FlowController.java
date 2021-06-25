package com.vantop.apitest.flow;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vantop.apitest.report.ReportService;
import com.vantop.apitest.report.model.Result;
import com.vantop.apitest.utils.TimeUtils;
import com.vantop.apitest.flow.model.FlowCase;
import com.vantop.apitest.flow.model.FlowLabel;
import com.vantop.apitest.mapper.*;
import com.vantop.apitest.system.model.ConfigKind;
import com.vantop.apitest.user.model.TestAccount;
import com.vantop.apitest.vo.Case;
import com.vantop.apitest.vo.CurrentUserVO;
import com.vantop.apitest.vo.FlowLabelVO;
import com.vantop.apitest.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/va")
public class FlowController {

    @Autowired
    FlowService flowService;

    @Autowired
    ReportService reportService;

    @Autowired
    ConfigKindMapper configKindMapper;

    @Autowired
    TestAccountMapper testAccountMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    DirMapper dirMapper;

    @Autowired
    SwaggerMapper swaggerMapper;

    /**
     * 根据uid查询标签列表
     *
     * @return
     */
    @GetMapping("/labelList/{uid}")
    public JSON labelList(@PathVariable("uid") Long uid, @RequestParam("kindId") Integer kindId,
                          @RequestParam("labelName") String labelName, @RequestParam("page") Integer page,
                          @RequestParam("limit") Integer limit) {

        IPage<FlowLabel> result = flowService.getLabelList(uid, kindId, labelName, page, limit);
        List<FlowLabel> labelList = result.getRecords();
        List<FlowLabelVO> labelVOS = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (FlowLabel fl : labelList) {
            FlowLabelVO flowLabelVO = new FlowLabelVO();
            BeanUtils.copyProperties(fl, flowLabelVO);
            TestAccount t = testAccountMapper.selectById(fl.getAccountId());
            if (null != t) {
                flowLabelVO.setAccount(t.getAccount());
            }
            flowLabelVO.setKindName(configKindMapper.selectById(fl.getKindId()).getKindName());
            flowLabelVO.setOwner(userMapper.selectById(fl.getUserId()).getUserName());
            flowLabelVO.setCreateTime(formatter.format(fl.getCreateTime()));
            labelVOS.add(flowLabelVO);
        }
        return ResultVO.success((long) labelVOS.size(), labelVOS);
    }

    @PostMapping("/addLabel")
    public JSON addLabel(@RequestBody FlowLabel flowLabel, HttpServletRequest request) {

        if (flowLabel.getId() != null) {
            HttpSession session = request.getSession();
            CurrentUserVO user = (CurrentUserVO) session.getAttribute("user");
            if (!flowLabel.getUserId().equals(user.getUid())) {
                return ResultVO.failure("只允许编辑自己的用例标签！");
            }
            return ResultVO.success(flowService.updateLabel(flowLabel));
        } else {
            flowLabel.setCreateTime(TimeUtils.nowTime());
            log.info(String.valueOf(flowLabel));
            return ResultVO.success(flowService.addLabel(flowLabel));
        }
    }

    @GetMapping("/flowCaseList/{uid}/{labelId}")
    public JSON flowCaseList(@PathVariable("labelId") Integer labelId, @PathVariable("uid") Long uid,
                          @RequestParam("page") Integer page, @RequestParam("limit") Integer limit) {
        IPage<FlowCase> result = flowService.getCaseListByLabelId(uid, labelId, page, limit);
        List<FlowCase> flowCaseList = result.getRecords();
        for (FlowCase f : flowCaseList) {
            ConfigKind dir = configKindMapper.selectById(f.getKindId());
            if (null != dir) {
                f.setKindName(dir.getKindName());
            }
        }
        return ResultVO.success(result.getTotal(), flowCaseList);
    }

    @GetMapping("/getUserLabel/{uid}")
    public JSON getUserLabel(@PathVariable("uid") Long uid) {
        List<FlowLabel> result = flowService.getLabelByUid(uid);
        return ResultVO.success(result);
    }

    @PostMapping("/addFlowCase")
    public JSON addFlowCase(@RequestBody FlowCase flowCase, HttpServletRequest request) {
        HttpSession session = request.getSession();
        CurrentUserVO user = (CurrentUserVO) session.getAttribute("user");
        FlowLabel flowLabel = flowService.getLabelById(flowCase.getLabelId());
        if (!flowLabel.getUserId().equals(user.getUid())) {
            return ResultVO.failure("只允许编辑自己的用例标签！");
        }
        if (flowCase.getId() != null) {
            return ResultVO.success(flowService.updateFlowCase(flowCase));
        } else {
            log.info(String.valueOf(flowCase));
            return ResultVO.success(flowService.addFlowCase(flowCase));
        }
    }

    @GetMapping("/importFlowCase")
    public JSON importFlowCase(@RequestParam("labelId") Integer labelId,@RequestParam("caseId") Integer caseId,
                               HttpServletRequest request) {
        HttpSession session = request.getSession();
        CurrentUserVO user = (CurrentUserVO) session.getAttribute("user");
        FlowLabel flowLabel = flowService.getLabelById(labelId);
        if (!flowLabel.getUserId().equals(user.getUid())) {
            return ResultVO.failure("只允许编辑自己的用例标签！");
        }
        if(flowService.importFlowCaseBySwagger(labelId, caseId,flowLabel.getLabelName())){
            return ResultVO.success("导入用例成功！");
        }else {
            return ResultVO.failure("导入用例失败！");
        }
    }

    @GetMapping("/flowCaseInfo/{id}")
    public JSON flowCaseInfo(@PathVariable("id") Integer id){
        FlowCase flowCase = flowService.getFlowCaseById(id);
        return ResultVO.success(flowCase);
    }

    @GetMapping("/deleteFlowCase/{id}")
    public JSON deleteFlowCase(@PathVariable("id") Integer id, @RequestParam("uid") Long uid){
        return ResultVO.success(flowService.deleteFlowCase(id, uid));
    }

    /**
     * 按场景标签执行用例
     * */
    @PostMapping("/runFlowCaseByLabelId")
    public JSON runFlowLabelList(@RequestParam("userName") String userName, @RequestParam("taskId") String taskId,
                                 @RequestParam(value = "labelIds", required = false) Integer[] labelIds, @RequestParam(value = "uid", required = false) Long uid){
        if(uid == null && labelIds.length==0){
            return ResultVO.failure("请选择需要执行的场景标签！");
        }
        if(uid != null){
            List<Integer> ids = flowService.getLabelListByUidAndStatus(uid);
            labelIds = new Integer[ids.size()];
            ids.toArray(labelIds);
        }
        Result flowResult0 = new Result();
        flowResult0.setTaskId(taskId);
        flowResult0.setRunStatus(0);//执行开始
        flowResult0.setTestUser(userName);
        flowResult0.setTestTime(TimeUtils.nowTime());
        flowResult0.setTaskType(2);
        reportService.saveResult(flowResult0, true);

        Result flowResult = reportService.getResultByTaskId(taskId);
        //统计所有标签的执行结果
        HashMap<Integer, Object> result = new HashMap<>();
        //按标签一个个执行用例
        for(Integer labelId: labelIds){
            result.putAll(flowService.runSingleFlowLabel(userName, taskId, labelId));
        }

        List<String> ids = new ArrayList();
        String logs = "";

        //分析结果并汇总统计
        for(Integer labelId: result.keySet()){
            Object value = result.get(labelId);
            //统计用例总数
            flowResult.setTotalCase(flowResult.getTotalCase()+flowService.getCaseListByLabelId(labelId).size());
            //结果分析
            String type = value.getClass().getName().toLowerCase();
            if(type.contains("string")){
                ids.add(labelId+":"+value);
                logs += "标签["+labelId+"]执行失败:"+value+" \n";
            }else {
                for(Case tt: (List<Case>) value){
                    if(tt.getResult().equals("pass")){
                        flowResult.setCasePass(flowResult.getCasePass()+1);
                    }else {
                        flowResult.setCaseFail(flowResult.getCaseFail()+1);
                    }
                }
            }
        }

        flowResult.setRunStatus(1);//执行结束
        flowResult.setTotalLabel(result.size());
        flowResult.setLabelFail(ids.size());
        flowResult.setLabelPass(result.size()-ids.size());
        flowResult.setLogs(logs==""?"执行成功":logs);
        reportService.saveResult(flowResult, false);

        String msg = "执行成功用例标签"+(flowResult.getLabelPass())+"个，失败"+flowResult.getLabelFail()+"个。\n";
        return ResultVO.success(msg+logs);
    }

}
