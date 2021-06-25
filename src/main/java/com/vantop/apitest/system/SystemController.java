package com.vantop.apitest.system;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vantop.apitest.system.model.ConfigKind;
import com.vantop.apitest.system.model.GlobalEnv;
import com.vantop.apitest.system.model.Mock;
import com.vantop.apitest.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/va")
public class SystemController {

    @Autowired
    SystemService systemService;

    @GetMapping("/kindListPage/")
    public JSON kindListPage(@RequestParam("page") Integer page, @RequestParam("limit") Integer limit){
        IPage<ConfigKind> result = systemService.getKindListPage(page, limit);
        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    @GetMapping("/kindList/{kindId}")
    public JSON kindList(@PathVariable("kindId") Integer kindId){

        return ResultVO.success(systemService.getKindList(kindId));
    }

    @GetMapping("/dirList/{kind}")
    public JSON dirList(@PathVariable("kind") Integer kind){
        return ResultVO.success(systemService.getDirList(kind));
    }

    @GetMapping("/globalList")
    public JSON globalList(@RequestParam("uid") Long uid,@RequestParam("page") Integer page, @RequestParam("limit") Integer limit) {
        IPage<GlobalEnv> result = systemService.getGlobalList(uid,page, limit);
        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    @PostMapping("/addGlobal")
    public JSON addGlobal(@RequestBody GlobalEnv global){
        return ResultVO.success(systemService.addGlobal(global));
    }

    @PostMapping("/useGlobal")
    public JSON useGlobal(@RequestParam("uid") Long uid, @RequestParam("id") Integer id, HttpSession session){
        try {
            if(systemService.useGlobal(uid,id)) {
                return ResultVO.success();
            }else{
                return ResultVO.failure();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    @PostMapping("/updateGlobal")
    public JSON updateGlobal(@RequestParam("id") Integer id,@RequestParam("field") String field,@RequestParam("value") String value,HttpSession session){
        try {
            systemService.updateGlobal(id,field,value);
            return ResultVO.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    @GetMapping("/delGlobal")
    public JSON delGlobal(@RequestParam("id") Integer id, @RequestParam("uid") Long uid) {
        return ResultVO.success(systemService.deleteGlobalEnv(id, uid));
    }


    @GetMapping("/globalRule")
    public JSON getGlobalRule() {
        return ResultVO.success(systemService.getGlobalRuleList());
    }

    @PostMapping("/mock")
    public JSON mock(@RequestBody List<Mock> list){
        try {
            if(systemService.mock(list)){
                return ResultVO.success();
            }else {
                return ResultVO.failure();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

}
