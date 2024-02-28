package com.atguigu.spzx.manager.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleUserMapper {
    //根据userId删除用户之前分配的角色数据
    public void deleteByUserId(Long userId) ;
    //遍历得到每个角色id
    public void doAssign(Long userId, Long roleId) ;

    //得到分配过的角色列表
    List<Long> selectRoleIdsByUserId(Long userId);
}
