package com.kr.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.code.kaptcha.Producer;
import com.kr.community.common.utils.R;
import com.kr.community.entity.SysLoginForm;
import com.kr.community.entity.User;
import com.kr.community.service.SysCaptchaService;
import com.kr.community.service.SysUserTokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
public class SysLoginController extends BaseController{

    @Autowired
    private SysUserTokenService sysUserTokenService;

    @Autowired
    private SysCaptchaService sysCaptchaService;

    //验证码的生成器
    @Autowired
    private Producer producer;


    @GetMapping("/capthcas.jpg")
    public void captchas(HttpServletResponse response, String uuid) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        //生成文字验证码
        //获取图片验证码
        BufferedImage image = sysCaptchaService.getCaptcha(uuid);

        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    /**
     * 登录
     */
    @PostMapping("/sys/login")
    public Map<String, Object> login(SysLoginForm form)throws IOException {
        //用户信息
        User user = userService.getOne(new QueryWrapper<User>().eq("userName",form.getUsername()));

        //账号不存在、密码错误
        if(user == null || !user.getPassword().equals(new Sha256Hash(form.getPassword(), user.getSalt()).toHex())) {
            return R.error("账号或密码不正确");
        }

        //账号锁定
        if(user.getStatus() == 0){
            return R.error("账号已被锁定,请联系管理员");
        }

        //生成token，并保存到数据库
        R r = sysUserTokenService.createToken(user.getId());
        return r;
    }

    @PostMapping("/sys/qclogin")
    public Map<String, Object> qclogin(String oauth_consumer_key,String access_token,String openid)throws IOException {
        return userService.qclogin(oauth_consumer_key, access_token, openid);
    }

    /**
     * 退出
     */
    @PostMapping("/sys/logout")
    public R logout() {
        try {
            sysUserTokenService.logout(getUserId());
        }catch (Exception e){
            e.printStackTrace();
            return R.error("登出异常");
        }

        return R.ok();
    }

    protected User getUser() {
        Subject sb = SecurityUtils.getSubject();
        User user =  (User) sb.getPrincipal();
        System.out.println("user = "+user.toString());
        return user;
    }

    protected Long getUserId() {
        return getUser().getId();
    }



}
