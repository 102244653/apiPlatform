package com.vantop.apitest.user;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vantop.apitest.utils.DESUtil;
import com.vantop.apitest.user.model.TestAccount;
import com.vantop.apitest.user.model.User;
import com.vantop.apitest.vo.CurrentUserVO;
import com.vantop.apitest.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/va")
public class UserController {

    @Autowired
    UserService userService;

    /**
     * 注册接口
     * */
    @PostMapping(value = "/register")
    public JSON register(@RequestBody User user){

        String str = userService.checkUser(user);
        if(str != null){
            return ResultVO.failure(str);
        }
        if(user.getPassWord().length()<6 || user.getPassWord().length()>10){
            return ResultVO.failure("请输入6到10位长度的密码！");
        }
        if(userService.addUser(user)){
            return ResultVO.success("用户注册成功", String.valueOf(user.getUid()));
        }else {
            return ResultVO.failure("用户注册失败");
        }
    }

    /**
     * 登录接口
     * */
    @PostMapping(value = "/login")
    public JSON login(@RequestParam("account") String account, @RequestParam("password") String password, HttpServletRequest request){
        try {
            DESUtil md5 = new DESUtil();
            User user = userService.loginByNameAndPassword(account);
            if(null != user && md5.encryptPassword(password).equals(user.getPassWord())){
                user.resetLoginTime();
                userService.updateUser(user);
                CurrentUserVO currentUserVO = new CurrentUserVO();
                BeanUtils.copyProperties(user, currentUserVO);
                String token = md5.createToken(user);
                currentUserVO.setToken(token);
                request.getSession().setAttribute("user", currentUserVO);
                return ResultVO.success("登录成功", currentUserVO);
            }else {
                return ResultVO.failure("账号或密码错误");
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure("账号或密码错误");
        }
    }

    /**
     * 根据uid查用户信息
     * */
    @GetMapping("/userInfo/{uid}")
    public JSON userInfo(@PathVariable("uid") Long uid){
        User user = userService.getUserByUid(uid);
        if(null!=user){
            return ResultVO.success(user);
        }else {
            User u = new User();
            u.setUid(uid);
            return ResultVO.failure("用户不存在", u);
        }

    }

    /**
     * 编辑测试账号
     * */
    @PostMapping("/editUser")
    public JSON editUser(@RequestBody User user){
        if(userService.updateUser(user)){
            return ResultVO.success(userService.getUserByUid(user.getUid()));
        }
        return ResultVO.failure("保存提交失败");
    }

    /**
     * 根据uid查询测试账号
     *
     * @return*/
    @GetMapping("/accountList/{uid}")
    public JSON accountList(@PathVariable("uid") Long uid, @RequestParam("page") Integer page, @RequestParam("limit") Integer limit){
        IPage<TestAccount> result = userService.getAccountListByUid(uid, page, limit);
        return ResultVO.success(result.getTotal(), result.getRecords());
    }

    @GetMapping("/getAccount/{uid}")
    public JSON getAccount(@PathVariable("uid") Long uid){
        List<TestAccount> result = userService.getAccountListByUid(uid);
        return ResultVO.success(result);
    }

    /**
     * 添加测试账号
     * */
    @PostMapping(value = "/addAccount")
    public JSON addAccount(@RequestBody TestAccount account){
        if(account.getPassword()=="" || account.getAccount()==""){
            return ResultVO.failure("账号和密码不能为空");
        }
        if(userService.checkAccountIsExist(account)){
            userService.addAccount(account);
            return ResultVO.success("添加成功");
        }else {
            return ResultVO.failure("账号已存在");
        }
    }

    /**
     * 删除测试账号
     *
     * @return*/
    @GetMapping(value = "/deleteAccount/{id}")
    public JSON deleteAccount(@PathVariable("id") Integer id){
        if(userService.deleteTestAccount(id)){
            return ResultVO.success("删除成功");
        }else {
            return ResultVO.success("删除失败");
        }
    }

    @PostMapping("/setDefault")
    public JSON setDefault(@RequestParam("uid") Long uid, @RequestParam("id") Integer id, HttpSession session){
        try {
            if(userService.setDefault(uid,id)) {
                return ResultVO.success();
            }else{
                return ResultVO.failure();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVO.failure();
        }
    }

    @GetMapping("/loginAccount")
    public JSON loginAccount(@RequestParam("id") Integer id, @RequestParam("uid") Long uid){
         return ResultVO.success(userService.checkTestAccount(id, uid));
    }

    @GetMapping("/checkStatus")
    public JSON checkStatus(){
         return ResultVO.success("登录状态正常！");
    }


}
