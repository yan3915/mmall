package com.mmall.service;

import com.mmall.Pojo.User;
import com.mmall.common.ServiceResponse;

/**
 * create by yanzhe
 * */
public interface IUserService {
    ServiceResponse<User> login(String username, String password);
    ServiceResponse<String> register(User user);
    ServiceResponse<String> checkVaild(String str,String type);
    ServiceResponse selectQuestion(String username);
    ServiceResponse<String> checkAnswer(String username,String question,String answer);
    ServiceResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);
    ServiceResponse<String> resetPassword(String passwordOld,String passwordNew,User user);
    ServiceResponse<User > updateInformation(User user);

    ServiceResponse<User> getInformation(Integer id);
}

