package com.kr.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kr.community.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lv-success
 * @since 2018-10-14
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户的所有权限
     * @param userId  用户ID
     */
    List<String> queryAllPerms(@Param("userId") Long userId);

    /**
     * 查询用户的所有角色
     * @param userId  用户ID
     */
    List<String> queryAllRoles(@Param("userId") Long userId);

    /**
     * 查询用户的所有菜单ID
     */
    List<Long> queryAllMenuId(Long userId);
}
