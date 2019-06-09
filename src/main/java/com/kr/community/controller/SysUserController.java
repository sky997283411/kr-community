package com.kr.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kr.community.common.utils.R;
import com.kr.community.entity.User;
import com.kr.community.service.SysCaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SysUserController extends BaseController{

    @Autowired
    private SysCaptchaService sysCaptchaService;

    @PostMapping("/sys/register")
    public R doRegister(User user, String captcha, String uuid) {
        log.info("uuid ="+uuid);

        boolean captchas = sysCaptchaService.validate(uuid, captcha);
        if(!captchas){
            return R.error("验证码不正确").put("code", 501);
        }
        R r = userService.register(user);
        return r;
    }

    @GetMapping("/public/checkNickname")
    public R checkNickname(String username){
        User po = userService.getOne(new QueryWrapper<User>().eq("username", username));
        if(po != null) {
            return R.error("用户已被注册");
        }else{
            return R.ok("用户未被注册").put("code", 200);
        }
    }

    @PostMapping("/sys/user/signToday")
    public R signToday(long userId){
        return  userService.signToday(userId);

    }

    @PostMapping("/sys/user/getSigndays")
    public R getSigndays(long userId){
        return userService.getSigndays(userId);

    }

    @PostMapping("/sys/user/getUserInfo")
    public R getUserInfo(long userId){
        return R.ok().put("data", userService.getUserInfo(userId));
    }

    @PostMapping("/sys/user/uploadUserImage")
    public R uploadUserImage(long userId,String imageUrl){
        userService.uploadUserImage(userId, imageUrl);
        return R.ok("success");
    }

    @PostMapping("/sys/user/updateUserInfo")
    public R updateUserInfo(User user){
        userService.updateUserInfo(user);
        return R.ok("success");
    }

    @PostMapping("/sys/user/queryAllRoles")
    public R queryAllRoles(long userId){
        return R.ok().put("data", userService.queryAllRoles(userId));
    }

}


