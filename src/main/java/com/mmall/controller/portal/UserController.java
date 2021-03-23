package com.mmall.controller.portal;
import com.mmall.Pojo.User;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpSession;
/**
 * created by yanzhe
 * */
@Controller
//请求地址放在requestMapping中
@RequestMapping("/user")

public class UserController {
    @Autowired
    private IUserService iUserService;
    /**
     * 用户登录
     *
     * **/
    @RequestMapping(value = "login.do",method = RequestMethod.POST)//post请求
    @ResponseBody //将返回值迅速序列化成json
    public ServiceResponse<User> login(String username, String password, HttpSession session){
        //调用service-mybatis-dao

        ServiceResponse<User> response=iUserService.login(username,password);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getdata());
        }
        return response;

    }
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> logout(HttpSession session){
       //退出就是remove掉seesion
        session.removeAttribute(Const.CURRENT_USER);
        return ServiceResponse.createBySuccess();
    }
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String > register(User user){
        return iUserService.register(user);

    }
    @RequestMapping(value = "checkValid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String > checkValid(String str,String type){
        return iUserService.checkVaild(str,type);
    }
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> getUserInfo(HttpSession session){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if(user!=null){
            return ServiceResponse.createBySuccess(user);
        }
        return  ServiceResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");

    }
    @RequestMapping(value = "forget_get_question.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }
    @RequestMapping(value = "forget_check_answer.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }
    @RequestMapping(value = "forget_reset_password.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String > forgetRestPassword(String username,String passwordNew, String forgetToken){
        return iUserService.forgetResetPassword(username ,passwordNew,forgetToken);
    }
    @RequestMapping(value = "reset_password.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> resetPassword(HttpSession session,String passwordOld,String password){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServiceResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,password,user);
    }
    @RequestMapping(value = "update_Information.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> update_Information(HttpSession session,User user){
        User currentUser=(User)session.getAttribute(Const.CURRENT_USER);
        if (currentUser==null){
            return ServiceResponse.createBySuccessMessage("用户未登录");

        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServiceResponse<User> response=iUserService.updateInformation(user);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getdata());
        }
        return response;
    }
    @RequestMapping(value = "get_Information.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> get_Information(HttpSession session){
        User currentUser=(User)session.getAttribute(Const.CURRENT_USER);
        if (currentUser==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需强制登录");

        }
        return iUserService.getInformation(currentUser.getId());
    }

}
