package com.vantop.apitest.swagger;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vantop.apitest.mapper.ConfigKindMapper;
import com.vantop.apitest.swagger.model.Swagger;
import com.vantop.apitest.swagger.model.SwaggerLog;
import com.vantop.apitest.system.model.ConfigKind;
import com.vantop.apitest.vo.CurrentUserVO;
import com.vantop.apitest.vo.ResultVO;
import com.vantop.apitest.vo.SwaggerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/va")
public class SwaggerController {

    @Autowired
    SwaggerService swaggerService;

    @Autowired
    ConfigKindMapper configKindMapper;

    @GetMapping("/swaggerList")
    public JSON swaggerList(@RequestParam("kind") Integer kind, @RequestParam("name") String name,@RequestParam("status") Integer status,
                            @RequestParam("page") Integer page, @RequestParam("limit") Integer limit){
        IPage<Swagger> result = swaggerService.getSwaggerPageByKind(kind, name,status, page, limit);
        List<SwaggerVO> swaggerVOList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(Swagger s:result.getRecords()){
            SwaggerVO swaggerVO = new SwaggerVO();
            BeanUtils.copyProperties(s, swaggerVO);
            swaggerVO.setUpdateTime(formatter.format(s.getUpdateTime()));
            ConfigKind configKind = configKindMapper.selectById(s.getKind());
            if(null!=configKind){
                swaggerVO.setKindName(configKind.getKindName());
            }
            swaggerVOList.add(swaggerVO);
        }
        return ResultVO.success(result.getTotal(), swaggerVOList);
    }

    @PostMapping("/editSwagger")
    public JSON editSwagger(@RequestParam("id") Integer id, @RequestParam("status") Integer status, HttpSession session){
        try {
            CurrentUserVO currentUserVO = (CurrentUserVO) session.getAttribute("user");
            swaggerService.updateSwaggerStatus(id, status, currentUserVO.getUid());
            return ResultVO.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    @GetMapping("/synSwagger")
    public JSON synSwagger(@RequestParam("kindId") Integer kindID){
        try{
            swaggerService.requestSwagger(kindID);
            return ResultVO.success();
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResultVO.failure();
    }

    @GetMapping("/swaggerLogList")
    public JSON swaggerLogList(@RequestParam("name") String name,@RequestParam("page") Integer page,
                               @RequestParam("limit") Integer limit){
        IPage<SwaggerLog> result = swaggerService.getSwaggerLogList(name, page, limit);

        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    @GetMapping("/readMockParams")
    public JSON readMockParams(@RequestParam("name") String name, @RequestParam("method") String method){
        return ResultVO.success(swaggerService.getParamsByMethodAndName(name.trim(), method.toLowerCase()));
    }

}
