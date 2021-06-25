package com.vantop.apitest.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vantop.apitest.common.CaseService;
import com.vantop.apitest.mapper.TestAccountMapper;
import com.vantop.apitest.mapper.UserMapper;
import com.vantop.apitest.system.SystemService;
import com.vantop.apitest.system.model.GlobalEnv;
import com.vantop.apitest.user.model.TestAccount;
import com.vantop.apitest.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    TestAccountMapper testAccountMapper;

    @Lazy
    @Autowired
    SystemService systemService;

    @Autowired
    CaseService caseService;

    public Long createUid(){
        Long time = System.currentTimeMillis();
        while (true){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", time);
            if(userMapper.selectCount(queryWrapper) != 0){
                time = time + 3;
                continue;
            }else {
                return time;
            }
        }
    }

    /**
     * 根据uid获取用户信息
     * */
    public User getUserByUid(Long uid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 检查账号是否已注册：手机号/邮箱
     * */
    public String checkUser(User user) {
        String str = null;
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", user.getPhone()).or().eq("email", user.getEmail());
        User user1 = userMapper.selectOne(queryWrapper);
        if(null == user1){
            return null;
        }
        if(!user1.getPhone().isEmpty()){
            str = user1.getPhone()+"已注册";
        }else if(!user1.getEmail().isEmpty()){
            str = user1.getEmail()+"已注册";
        }
        return str;
    }

    public boolean addUser(User user) {
        user.setUid(this.createUid());
        user.resetPassWord();
        try{
            userMapper.insert(user);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean updateUser(User user){
        log.info("更新用户："+user);
        try{
            userMapper.updateById(user);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public User loginByNameAndPassword(String account) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", account).or().eq("email", account);
        User user = userMapper.selectOne(queryWrapper);
        return user;
    }

    public IPage<TestAccount> getAccountListByUid(Long uid, Integer page, Integer limit) {
        //分页配置
        IPage<TestAccount> accountIPage = new Page<>(page, limit);
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        IPage<TestAccount> result = testAccountMapper.selectPage(accountIPage, queryWrapper);
        return result;
    }

    public List<TestAccount> getAccountListByUid(Long uid) {
        //分页配置
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        List<TestAccount> result = testAccountMapper.selectList( queryWrapper);
        return result;
    }

    public boolean checkAccountIsExist(TestAccount account) {
        QueryWrapper<TestAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", account.getUid()).eq("account", account.getAccount());
        if(null ==testAccountMapper.selectOne(queryWrapper)){
            return true;
        }
        return false;
    }

    public void addAccount(TestAccount account) {
        testAccountMapper.insert(account);
    }

    public boolean deleteTestAccount(Integer id) {
        try {
            testAccountMapper.deleteById(id);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDefault(Long uid,Integer id) {
        try {
            UpdateWrapper<TestAccount> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).set("isDefault",0);
            testAccountMapper.update(null, updateWrapper);

            UpdateWrapper<TestAccount> updateWrapper2 = new UpdateWrapper<>();
            updateWrapper2.eq("id", id).set("isDefault",1);
            testAccountMapper.update(null, updateWrapper2);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String checkTestAccount(Integer id, Long uid) {
        TestAccount testAccount = testAccountMapper.selectById(id);
        GlobalEnv globalEnv = systemService.getEnvByUidAndPlatform(uid, testAccount.getPlatform());
        if(globalEnv==null){
            return "请先设置测试环境！";
        }
        try{
            caseService.loginSaas(globalEnv, testAccount);
            if(!testAccountMapper.selectById(id).getLoginDetail().endsWith("bearer ")){
                return "登录成功";
            }else {
                return "登录失败";
            }
        }catch (Exception e){
            return "登录失败";
        }
    }
}
