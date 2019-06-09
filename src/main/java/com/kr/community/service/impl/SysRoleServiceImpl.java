package com.kr.community.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kr.community.common.utils.Constant;
import com.kr.community.config.exception.RRException;
import com.kr.community.dao.SysRoleMapper;
import com.kr.community.entity.SysRoleEntity;
import com.kr.community.service.SysRoleService;
import com.kr.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年9月18日 上午9:45:12
 */
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

	@Autowired
	private UserService userService;


	@Override
	public void update(SysRoleEntity role) {

	}

	@Override
	public void deleteBatch(Long[] roleIds) {

	}

	@Override
	public List<Long> queryRoleIdList(Long createUserId) {
		return baseMapper.queryRoleIdList(createUserId);
	}

	/**
	 * 检查权限是否越权
	 */
	private void checkPrems(SysRoleEntity role){
		//如果不是超级管理员，则需要判断角色的权限是否超过自己的权限
		if(role.getCreateUserId() == Constant.SUPER_ADMIN){
			return ;
		}
		
		//查询用户所拥有的菜单列表
		List<Long> menuIdList = userService.queryAllMenuId(role.getCreateUserId());
		
		//判断是否越权
		if(!menuIdList.containsAll(role.getMenuIdList())){
			throw new RRException("新增角色的权限，已超出你的权限范围");
		}
	}
}
