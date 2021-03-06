package com.mmall.controller.backend;

import com.mmall.Pojo.User;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/user")
//后台管理
public class UserManageController {
    @Autowired
    private IUserService iUserService;
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> longin(String username, String password, HttpSession session){
         ServiceResponse<User> response=iUserService.login(username,password);
         if(response.isSuccess()){
             User user=response.getdata();
             if(user.getRole()== Const.Role.ROLE_ADMIN){
                 //说明登录管理员后台
                 session.setAttribute(Const.CURRENT_USER,user);
                 return response;
             }
             else {
                 return ServiceResponse.createByErrorMessage("不是管理员，无法登录");
             }

         }
        return response;
    }

}
