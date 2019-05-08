package cn.itcast.core.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map<String,Object> showName(HttpServletRequest request){

        //获取session域中的用户
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String,Object> map=new HashMap<>();
        map.put("loginName",name);
        //map.put("time",new Date());
        return map;
    }
}
