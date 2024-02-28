package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.SysRoleMapper;
import com.atguigu.spzx.manager.mapper.SysRoleUserMapper;
import com.atguigu.spzx.manager.service.SysRoleService;
import com.atguigu.spzx.model.dto.system.SysRoleDto;
import com.atguigu.spzx.model.entity.system.SysRole;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;
    //角色列表的方法
    @Override
    public PageInfo<SysRole> findByPage(Integer current, Integer limit, SysRoleDto sysRoleDto) {
        //设置分页参数
        PageHelper.startPage(current, limit);
        //根据条件查询所有数据
        List<SysRole> list=sysRoleMapper.findByPage(sysRoleDto);
        //封装PageInfo对象
        PageInfo<SysRole> pageInfo=new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public void saveSysRole(SysRole sysRole) {
        sysRoleMapper.save(sysRole);
    }

    @Override
    public void updateSysRole(SysRole sysRole) {
        sysRoleMapper.update(sysRole);
    }

    @Override
    public void deleteSysRole(String rowId) {
        sysRoleMapper.delete(rowId);
    }

    @Override
    public Map<String, Object> findAll(Long userId) {
        //1.查询所有角色
        List<SysRole> list=sysRoleMapper.findAll();
        //2. 分配过的角色列表
        List<Long> roleIds = sysRoleUserMapper.selectRoleIdsByUserId(userId);

        Map<String,Object> map = new HashMap<>();
        map.put("allRolesList",list);
        map.put("sysUserRoles",roleIds);
        return map;
    }
}
