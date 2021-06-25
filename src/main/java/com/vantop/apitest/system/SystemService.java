package com.vantop.apitest.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vantop.apitest.mapper.*;
import com.vantop.apitest.single.SingleService;
import com.vantop.apitest.swagger.model.Swagger;
import com.vantop.apitest.system.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SystemService {

    @Autowired
    ConfigKindMapper configKindMapper;

    @Autowired
    DirMapper dirMapper;

    @Autowired
    GlobalEnvMapper globalEnvMapper;

    @Lazy
    @Autowired
    SingleService singleService;

    @Autowired
    GlobalRuleMapper globalRuleMapper;

    @Autowired
    SwaggerMapper swaggerMapper;

    @Autowired
    DBConfigMapper dbConfigMapper;


    /**
     * 获取目录列表，会分页
     */
    public IPage<ConfigKind> getKindListPage(Integer page, Integer limit) {
        IPage<ConfigKind> ipage = new Page<>(page, limit);
        IPage<ConfigKind> result = configKindMapper.selectPage(ipage, null);
        return result;
    }

    public List<ConfigKind> getKindList(Integer kindId) {
        List<ConfigKind> result = new ArrayList<>();
        if(kindId!=0){
            QueryWrapper<ConfigKind> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("kindId", kindId);
            result = configKindMapper.selectList(queryWrapper);
        }else {
            result = configKindMapper.selectList(null);
        }
        return result;
    }

    /**
     * 查询模块下的自定义分类
     */
    public List<Dir> getDirList(int kindId) {
        QueryWrapper<Dir> query = new QueryWrapper<>();
        query.eq("kindId", kindId);
        List<Dir> result = dirMapper.selectList(query);
        return result;
    }

    public IPage<GlobalEnv> getGlobalList(Long uid, Integer page, Integer limit) {
        IPage<GlobalEnv> iPage = new Page<>(page, limit);
        QueryWrapper<GlobalEnv> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        IPage<GlobalEnv> result = globalEnvMapper.selectPage(iPage, queryWrapper);
        return result;
    }

    public String addGlobal(GlobalEnv global) {
        log.info(String.valueOf(global));
        QueryWrapper<GlobalEnv> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", global.getUid())
                    .eq("remoteAddress",global.getRemoteAddress());
        GlobalEnv tt = globalEnvMapper.selectOne(queryWrapper);
        if(tt==null){
            globalEnvMapper.insert(global);
            return "添加成功";
        }else {
            return "当前remoteAddress已存在！"; }

    }

    public boolean useGlobal(Long uid, Integer id) {
        try {
            UpdateWrapper<GlobalEnv> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).set("status", 0);
            globalEnvMapper.update(null, updateWrapper);

            UpdateWrapper<GlobalEnv> updateWrapper2 = new UpdateWrapper<>();
            updateWrapper2.eq("id", id).set("status", 1);
            globalEnvMapper.update(null, updateWrapper2);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateGlobal(Integer id, String field, String value) {
        try {
            QueryWrapper<GlobalEnv> query = new QueryWrapper<>();
            query.eq("id", id);
            GlobalEnv r = globalEnvMapper.selectOne(query);
            singleService.setValue(r, r.getClass(), field, GlobalEnv.class.getDeclaredField(field).getType(), value);

            UpdateWrapper<GlobalEnv> update = new UpdateWrapper<>();
            update.eq("id", id);
            globalEnvMapper.update(r, update);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<GlobalRule> getGlobalRuleList() {
        return globalRuleMapper.selectList(null);
    }

    public boolean mock(List<Mock> list) {
        try {
            for (Mock mock : list) {
                log.info("mock==>"+mock.toString());
                QueryWrapper<Swagger> query = new QueryWrapper<>();
                query.eq("name", mock.getName());
                query.eq("method", mock.getMethod());
                Swagger r = swaggerMapper.selectOne(query);
                if (r != null) {
                    UpdateWrapper<Swagger> update = new UpdateWrapper<>();
                    update.eq("name", mock.getName());
                    update.eq("method", mock.getMethod());
                    r.setParams(mock.getParams());
                    swaggerMapper.update(r, update);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public GlobalEnv getEnvByUidAndPlatform(Long uid, Integer platForm){
        QueryWrapper<GlobalEnv> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("platForm", platForm)
                .eq("status", 1);
        GlobalEnv env = globalEnvMapper.selectOne(queryWrapper);
        //明峰未设置则取默认第一条
        if(env==null && platForm ==1){
            env = globalEnvMapper.selectById(1);
        }
        return env;
    }

    public String deleteGlobalEnv(Integer id, Long uid) {
        GlobalEnv env = globalEnvMapper.selectById(id);
        if(env!=null && env.getUid().equals(uid)){
            QueryWrapper<GlobalEnv> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", uid);
            if(env.getStatus()==1){
                return "不允许删除已起用的测试环境！";
            }
            if(globalEnvMapper.selectCount(queryWrapper)==1){
                return "不允许删除，至少添加一个测试环境！";
            }
            globalEnvMapper.deleteById(id);
            return "删除成功！";
        }else {
            return "只能删除自己的环境！";
        }
    }

    public DBConfig getDBConfigByPlatform(Integer platform){
        //初始化sql执行器
        QueryWrapper<DBConfig> qdb = new QueryWrapper<>();
        qdb.eq("platform", platform);
        DBConfig dbConfig = dbConfigMapper.selectOne(qdb);
        return dbConfig;
    }
}
