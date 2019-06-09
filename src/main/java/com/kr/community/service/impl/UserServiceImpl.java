package com.kr.community.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kr.community.common.utils.DateUtils;
import com.kr.community.common.utils.HttpTemplate;
import com.kr.community.common.utils.R;
import com.kr.community.common.utils.RedisUtil;
import com.kr.community.dao.UserMapper;
import com.kr.community.entity.User;
import com.kr.community.service.SysUserTokenService;
import com.kr.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lv-success
 * @since 2018-10-14
 */
@Slf4j
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserTokenService sysUserTokenService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public R register(User user) {

        if(StringUtils.isEmpty(user.getPassword())
                || StringUtils.isEmpty(user.getUsername())) {
            return R.error("必要字段不能为空");
        }

        User po = this.getOne(new QueryWrapper<User>().eq("username", user.getEmail()));
        if(po != null) {
            return R.error("用户已被注册");
        }

        //sha256加密
        String salt = RandomStringUtils.randomAlphanumeric(20);

        po = new User();
        po.setEmail(user.getEmail());
        po.setSalt(salt);
        po.setPassword(new Sha256Hash(user.getPassword(), salt).toHex());
        po.setCreated(new Date());
        po.setUsername(user.getUsername());
        po.setAvatar("/images/avatar/default.png");
        po.setPoint(0);
        po.setStatus(1);
        //注册送100飞吻
        po.setKissCount(100);

        return this.save(po)? R.ok().put("data","注册成功") : R.error("注册失败");
    }

    @Override
    public R signToday(long userId) {
        Date today = new Date();
        String todayformat= DateUtils.format(today);
        if(redisUtil.sHasKey("user:sign:" + userId, todayformat)){
            //今天已经签到了
            return R.ok().put("data","已签到").put("code",1000);
        }else{
            redisUtil.sSet("user:sign:" + userId, todayformat);
            redisUtil.incr("user:signdays:" + userId,1);

            //签到成功，用户获得飞吻奖励
            User user = userMapper.selectById(userId);
            int kiss = user.getKissCount();
            int signdays = (int) redisUtil.get("user:signdays:" + userId);
            int award = 10;
            if(signdays<10){
                kiss = kiss + 10;
            }else if(signdays>=10 && signdays < 30){
                kiss = kiss + 20;
                award=20;
            }else if(signdays >= 30){
                kiss = kiss + 30;
                award=30;
            }
            user.setKissCount(kiss);
            userMapper.updateById(user);

            return R.ok().put("data","签到成功，奖励"+award+"飞吻！");
        }
    }

    @Override
    public R getSigndays(long userId) {
        Date today = new Date();
        String todayformat= DateUtils.format(today);
        Date yesterday = DateUtils.addDateDays(today,-1);
        String yesterdayFormat = DateUtils.format(yesterday);
        String singkey = "user:sign:" + userId;
        String signdaysKey = "user:signdays:" + userId;
        Object days = new Object();
        if(redisUtil.sHasKey(singkey,todayformat)){
            if(!redisUtil.sHasKey(singkey, yesterdayFormat)){
                //今天已签到，昨天未签到，设置连续签到天数重置为1
                redisUtil.set(signdaysKey, 1);
            }
            //今天已签到，昨天也签到，签到天数不用管
            days = redisUtil.get(signdaysKey);
            return R.ok().put("data",days ==null? 0: days).put("code",1000);
        }else{
            if(!redisUtil.sHasKey(singkey, yesterdayFormat)){
                //今天未签到，昨天也未签到，设置连续签到天数重置为0
                redisUtil.set(signdaysKey, 0);
            }
            days = redisUtil.get(signdaysKey);
            return R.ok().put("data",days ==null? 0: days).put("code",1001);
        }
    }

    @Override
    public Map<String, Object> getUserInfo(long userId) {
        Map<String,Object> result = new HashMap<>(10);
        User user = this.getById(userId);
        String imgSrc = user.getImage();
        String gender = user.getGender();
        int vipLevel = user.getVipLevel();
        String honor = user.getHonor();
        String sign = user.getSign();
        String created = DateUtils.format(user.getCreated());
        String home = user.getHome();
        result.put("imgSrc",imgSrc);
        result.put("gender",gender);
        result.put("vipLevel",vipLevel);
        result.put("honor",honor);
        result.put("sign",sign);
        result.put("created",created);
        result.put("home",home);
        result.put("kissCount",user.getKissCount());
        result.put("username",user.getUsername());
        result.put("email",user.getEmail());
        return result;
    }

    @Override
    public List<String> queryAllPerms(Long userId) {
        return userMapper.queryAllPerms(userId);
    }

    @Override
    public List<Long> queryAllMenuId(Long userId) {
        return userMapper.queryAllMenuId(userId);
    }

    @Override
    public void uploadUserImage(long userId, String imageUrl) {
        User updateUser = userMapper.selectById(userId);
        updateUser.setImage(imageUrl);
        userMapper.updateById(updateUser);
    }

    @Override
    public void updateUserInfo(User user) {
        long userId = user.getId();
        User mine = this.userMapper.selectById(userId);
        mine.setEmail(user.getEmail());
        mine.setGender(user.getGender());
        mine.setHome(user.getHome());
        mine.setSign(user.getSign());
        userMapper.updateById(mine);
    }

    @Override
    public List<String> queryAllRoles(long userId) {
        return userMapper.queryAllRoles(userId);
    }

    @Override
    public com.kr.community.common.utils.R qclogin(String oauth_consumer_key, String access_token, String openid) {
        JSONObject json= HttpTemplate.doGetqq(oauth_consumer_key,access_token,openid);
        User user = this.getOne(new QueryWrapper<User>().eq("openid",openid));
        if(null == user){
            //第一次登陆，数据存入数据库
            User qqUser = new User();
            qqUser.setHome(json.getString("province")+json.getString("city"));
            qqUser.setImage(json.getString("figureurl_qq_2"));
            boolean boy = "男".equals(json.getString("gender"));
            qqUser.setGender(boy?"male":"female");
            qqUser.setBirthday(json.getString("year"));
            qqUser.setOpenid(openid);
            qqUser.setUsername(json.getString("nickname"));
            qqUser.setKissCount(100);
            qqUser.setCreated(new Date());
            qqUser.setModified(new Date());
            this.save(qqUser);
            return sysUserTokenService.createToken(qqUser.getId());
        }else{
            return sysUserTokenService.createToken(user.getId());
        }
    }

    @Override
    public void join(Map<String, Object> map, String field) {

        if(map == null || map.get(field) == null) return;

        Map<String, Object> joinColumns = new HashMap<>();

        //字段的值
        String linkfieldValue = map.get(field).toString();

        User user = this.getById(linkfieldValue);

        joinColumns.put("username", user.getUsername());
        joinColumns.put("email", user.getEmail());
        joinColumns.put("avatar", user.getAvatar());
        joinColumns.put("id", user.getId());
        joinColumns.put("vipLevel", user.getVipLevel());

        map.put("author", joinColumns);
    }
}
