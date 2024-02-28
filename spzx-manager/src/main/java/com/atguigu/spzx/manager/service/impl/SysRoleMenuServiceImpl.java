package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.SysRoleMenuMapper;
import com.atguigu.spzx.manager.service.SysMenuService;
import com.atguigu.spzx.manager.service.SysRoleMenuService;
import com.atguigu.spzx.model.dto.system.AssginMenuDto;
import com.atguigu.spzx.model.entity.system.SysMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysRoleMenuServiceImpl implements SysRoleMenuService {
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysMenuService sysMenuService;

    //查询所有菜单和查询角色分配过的菜单id列表
    @Override
    public Map<String, Object> findSysRoleMenuByRoleId(Long roleId) {
        //查询所有菜单
        List<SysMenu> sysMenuList = sysMenuService.findNodes();

        //查询角色分配过的菜单id列表
        List<Long> roleMenuIds = sysRoleMenuMapper.findSysRoleMenuByRoleId(roleId);
        Map<String, Object> map=new HashMap<>();
        map.put("sysMenuList",sysMenuList);
        map.put("roleMenuIds",roleMenuIds);
        return map;
    }


    @Override
    public void doAssign(AssginMenuDto assginMenuDto) {
        //删除角色  分配菜单数据
        sysRoleMenuMapper.deleteByRoleId(assginMenuDto.getRoleId());
        //保存分配数据
        List<Map<String,Number>> menuInfo = assginMenuDto.getMenuIdList();
        if(menuInfo!=null&&menuInfo.size()>0){
            sysRoleMenuMapper.doAssign(assginMenuDto);
        }
    }
}
