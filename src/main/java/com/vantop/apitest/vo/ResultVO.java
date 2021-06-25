package com.vantop.apitest.vo;
import com.alibaba.fastjson.JSON;
import lombok.Data;


@Data
public class ResultVO<T> {
    private Integer code;
    private String msg;
    private Long count;
    private T data;

    public static<T> JSON success(){
        ResultVO<T> success = new ResultVO<>();
        success.setCode(0);
        success.setMsg("操作成功");
        success.setCount(0L);
        success.setData(null);
        return (JSON) JSON.toJSON(success);
    }

    public static<T> JSON success(String msg){
        ResultVO<T> success = new ResultVO<>();
        success.setCode(0);
        success.setMsg(msg);
        success.setCount(0L);
        success.setData(null);
        return (JSON) JSON.toJSON(success);
    }

    public static<T> JSON success(T data){
        ResultVO<T> success = new ResultVO<>();
        success.setCode(0);
        success.setMsg("操作成功");
        success.setCount(0L);
        success.setData(data);
        return (JSON) JSON.toJSON(success);
    }

    public static<T> JSON success(String msg, T data){
        ResultVO<T> success = new ResultVO<>();
        success.setCode(0);
        success.setMsg(msg);
        success.setCount(0L);
        success.setData(data);
        return (JSON) JSON.toJSON(success);
    }

    public static<T> JSON success(Long count, T data){
        ResultVO<T> success = new ResultVO<>();
        success.setCode(0);
        success.setMsg("操作成功");
        success.setCount(count);
        success.setData(data);
        return (JSON) JSON.toJSON(success);
    }

    public static<T> JSON success(String msg, Long count, T data){
        ResultVO<T> success = new ResultVO<>();
        success.setCode(0);
        success.setMsg(msg);
        success.setCount(count);
        success.setData(data);
        return (JSON) JSON.toJSON(success);
    }

    public static<T> JSON failure(){
        ResultVO<T> failure = new ResultVO<>();
        failure.setCode(9999);
        failure.setMsg("操作失败");
        failure.setCount(0L);
        failure.setData(null);
        return (JSON) JSON.toJSON(failure);
    }

    public static<T> JSON failure(String msg){
        ResultVO<T> failure = new ResultVO<>();
        failure.setCode(9999);
        failure.setMsg(msg);
        failure.setCount(0L);
        failure.setData(null);
        return (JSON) JSON.toJSON(failure);
    }

    public static<T> JSON failure(String msg, Long count, T data){
        ResultVO<T> failure = new ResultVO<>();
        failure.setCode(9999);
        failure.setMsg(msg);
        failure.setCount(count);
        failure.setData(data);
        return (JSON) JSON.toJSON(failure);
    }

    public static<T> JSON failure(String msg, T data){
        ResultVO<T> failure = new ResultVO<>();
        failure.setCode(9999);
        failure.setMsg(msg);
        failure.setCount(0L);
        failure.setData(data);
        return (JSON) JSON.toJSON(failure);
    }


}

//示例
//@Override
//public ResultVO<UserVO> getUser(){
//    ResultVO resultVO = new ResultVO();
//    resultVO.setCode(0000);
//    resultVO.setMsg("请求成功");
//    //返回单个对象信息
//    UserVO userVO = new UserVO();
//    //返回多个对象信息使用数组
//    List<UserVO> userVOList = new ArrayList<>();
//    for(UserVO user: userList){
//        UserVO userVO1 = new UserVO();
//        //复制user到userVO，相同属性和值的拷贝
//        BeanUtils.copyProperties(user, userVO1);
//        userVOList.add(userVO1);
//    }
//    //返回接口结果,转为json即可
//    return resultVO;
//}
