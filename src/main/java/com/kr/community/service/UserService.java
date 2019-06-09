package com.kr.community.service;


import com.kr.community.common.utils.R;
import com.kr.community.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lv-success
 * @since 2018-10-14
 */
public interface UserService extends BaseService<User>{

    /**
     * 注册
     * @param user
     * @return
     */
    R register(User user);

    R signToday(long userId);

    R getSigndays(long userId);

    Map<String, Object> getUserInfo(long userId);

    List<String> queryAllPerms(@Param("userId") Long userId);

    /**
     * 查询用户的所有菜单ID
     */
    List<Long> queryAllMenuId(Long userId);

    void uploadUserImage(long userId, String imageUrl);

    void updateUserInfo(User user);

    List<String> queryAllRoles(long userId);

    R qclogin(String oauth_consumer_key, String access_token, String openid);

}
