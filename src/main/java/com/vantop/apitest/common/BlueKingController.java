package com.vantop.apitest.common;
import com.alibaba.fastjson.JSON;
import com.vantop.apitest.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/va")
public class BlueKingController {

    @PostMapping("/runByBlueKing")
    public JSON  runByBlueKing(@RequestParam("platForm") Integer platForm, @RequestParam("taskId") String taskId,
                               @RequestParam("runType") Integer runType, @RequestParam("modules") List<Integer> modules,
                               @RequestParam("host") String host, @RequestParam("account") String account){

        return ResultVO.success();
    }

    @GetMapping("/blueKingRunInfo/{taskId}")
    public JSON blueKingRunInfo(@PathVariable("taskId") String taskId){

        return ResultVO.success();
    }

}
