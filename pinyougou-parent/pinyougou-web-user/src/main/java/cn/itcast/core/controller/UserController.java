package cn.itcast.core.controller;

import cn.itcast.common.utils.PhoneFormatCheckUtils;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.PatternSyntaxException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/sendCode")
    public Result sendCode(String phone){


        try {
            if (PhoneFormatCheckUtils.isPhoneLegal(phone)) {

                userService.sendCode(phone);
            } else {
                return new Result(false,"手机号不正确");
            }
            return new Result(true,"发送成功");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return new Result(false,"发送失败");
        }
    }


    @RequestMapping("/add")
    public Result add(@RequestBody User user, String smscode){


        try {
            userService.add(user,smscode);
            return new Result(true,"注册成功");

        } catch (RuntimeException e) {
            return new Result(false,e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"注册失败");
        }
    }
}
