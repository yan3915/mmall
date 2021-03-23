package com.mmall.service.impl;

import com.mmall.Pojo.User;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);//检查用户名存在不存在
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
        //MD5登录
        //todo 密码登录
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServiceResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);//把密码指控
        return ServiceResponse.createBySuccess("登陆成功", user);

    }

    //注册的时候也需要校验，校验用户名是否存在
    public ServiceResponse<String> register(User user) {
        /**
         int resultCount=userMapper.checkUsername(user.getUsername());
         if(resultCount>0){
         return ServiceResponse.createByErrorMessage("用户名已存在");
         }
         */
        ServiceResponse vaildResponse = this.checkVaild(user.getUsername(), Const.USERNAME);
        if (!vaildResponse.isSuccess()) {
            return vaildResponse;
        }
        vaildResponse = this.checkVaild(user.getEmail(), Const.EMAIL);
        if (!vaildResponse.isSuccess()) {
            return vaildResponse;
        }

        /**
         resultCount=userMapper.checkEmail(user.getEmail());
         if(resultCount>0){
         return ServiceResponse.createByErrorMessage("邮箱已注册");
         }
         */
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("注册失败");
        }
        return ServiceResponse.createBySuccessMessage("注册成功");

    }

    public ServiceResponse<String> checkVaild(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            //开始校验
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("用户名已经存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("用户已经存在");
                }
            }

        } else {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccessMessage("校验成功");
    }

    public ServiceResponse selectQuestion(String username) {
        ServiceResponse vaildResponse = this.checkVaild(username, Const.USERNAME);
        if (vaildResponse.isSuccess()) {
            //用户不存在
            return ServiceResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("找回密码的问题是空的");
    }


    public ServiceResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明问题及问题答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServiceResponse.createBySuccess(forgetToken);
        }
        return ServiceResponse.createByErrorMessage("问题答案错误");
    }

    public ServiceResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServiceResponse.createByErrorMessage("参数错误，token需要传递");

        }
        ServiceResponse vaildResponse = this.checkVaild(username, Const.USERNAME);
        if (vaildResponse.isSuccess()) {
            //用户不存在
            return ServiceResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServiceResponse.createByErrorMessage("token无效或者过期");

        }
        if (StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
                if (rowCount > 0) {
                    return ServiceResponse.createBySuccessMessage("修改密码成功");
                }


        } else {
            return ServiceResponse.createByErrorMessage("token 错误，请重新重置密码");
        }

        return ServiceResponse.createByErrorMessage("修改密码失败");
    }
    public ServiceResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止越权
        int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount==0){
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updataCount=userMapper.updateByPrimaryKeySelective(user);
        if(updataCount>0){
            return ServiceResponse.createBySuccessMessage("密码更新成功");
        }
        return ServiceResponse.createByErrorMessage("密码更新失败");
    }
    public  ServiceResponse<User > updateInformation(User user){
        //user_name不能更新
        //email进行一个校验，校验新的email是否已经存在,不能时当前的用户
        int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return ServiceResponse.createBySuccessMessage("email已经存在，请更换email，再次尝试");


        }
        User updateuser=new User();
        updateuser.setId(user.getId());
        updateuser.setEmail(user.getEmail());
        updateuser.setPhone(user.getPhone());
        updateuser.setQuestion(user.getQuestion());
        int updateCount=userMapper.updateByPrimaryKeySelective(updateuser);
        if(updateCount>0){
            return ServiceResponse.createBySuccess("更新个人信息成功",updateuser);
        }
        return ServiceResponse.createByErrorMessage("更新个人失败");
    }
    public ServiceResponse<User> getInformation(Integer userId){
        User user =userMapper.selectByPrimaryKey(userId);
        if(user==null){
            return  ServiceResponse.createByErrorMessage("找不到当前用户");

        }
        user.setPassword(StringUtils.EMPTY);
             return   ServiceResponse.createBySuccess(user);
    }
}
