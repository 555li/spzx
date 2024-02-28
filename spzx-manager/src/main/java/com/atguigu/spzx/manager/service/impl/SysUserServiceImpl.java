package com.atguigu.spzx.manager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.manager.mapper.SysRoleUserMapper;
import com.atguigu.spzx.manager.mapper.SysUserMapper;
import com.atguigu.spzx.manager.service.SysUserService;
import com.atguigu.spzx.model.dto.system.AssginRoleDto;
import com.atguigu.spzx.model.dto.system.LoginDto;
import com.atguigu.spzx.model.dto.system.SysUserDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.LoginVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public LoginVo login(LoginDto loginDto) {
        //1 获取输入验证码和存储到redis的key名称 LoginDto直接获取
        String captcha = loginDto.getCaptcha();
        String key = loginDto.getCodeKey();
        System.out.println(captcha);
        System.out.println("user:validate" + key);
        //2 根据key获取存储在redis中的验证码值
        String redisCode = redisTemplate.opsForValue().get("user:validate" + key).replace("\"", "");
        System.out.println(redisCode);
        //3 比较输入的验证码和redis存储验证码是否一致
        if (StrUtil.isEmpty(redisCode) || !StrUtil.equalsIgnoreCase(captcha, redisCode)) {
            throw new GuiguException(ResultCodeEnum.VALIDATECODE_ERROR);
        }
        //4 如果不一致，提示用户校验失败
        //5 如果一直，删除redis中的验证码
        redisTemplate.delete("user:validate" + key);

        //1 获取提交用户名，loginDto获取到
        String username = loginDto.getUserName();
        //2 根据用户名查询数据库表 sys_user表
        SysUser sysUser = sysUserMapper.selectUserInfoByUsername(username);
        //3 如果根据用户名查不到对应信息，用户不存在，返回错误信息
        if (sysUser == null) {
//            throw new RuntimeException("用户名不存在");
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }
        //4 如果根据用户名查询到用户信息，用户存在
        //5 获取输入的密码，比较输入的密码与数据库密码是否一致
        String database_password = sysUser.getPassword();
        //把输入的密码进行加密，在比较数据库密码，md5
        String input_password = DigestUtils.md5DigestAsHex(loginDto.getPassword().getBytes());

        if (!input_password.equals(database_password)) {
//            throw new RuntimeException("密码不正确");
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        //6如果密码一致，登陆成功，如果密码不一致，登录失败
        //7 登陆成功，生成用户唯一标识token
        String token = UUID.randomUUID().toString().replace("-", "");

        //8 把登录成功的用户信息放到redis里面
        redisTemplate.opsForValue().set("user:login" + token,
                JSON.toJSONString(sysUser),
                7,
                TimeUnit.DAYS);

        //8 返回loginVo对象
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        return loginVo;
    }
    //获取当前登录用户信息
    @Override
    public SysUser getUserInfo(String token) {
        String userJson = redisTemplate.opsForValue().get("user:login" + token);
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        return sysUser ;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("user:login" + token);
    }
    //分页查询接口
    @Override
    public PageInfo<SysUser> findByPage(Integer pageNum, Integer pageSize, SysUserDto sysUserDto) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> list = sysUserMapper.selectByPage(sysUserDto) ;
        PageInfo<SysUser> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    //添加用户
    @Override
    public void saveSysUser(SysUser sysUser) {
        //1.判断用户名不能重复
        String userName = sysUser.getUserName();
        SysUser dbSysUser = sysUserMapper.selectUserInfoByUsername(userName);
        if (dbSysUser != null) {
            throw new GuiguException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }
        //2.输入密码进行md5加密
        String password = sysUser.getPassword();
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        sysUser.setPassword(md5Password);
        //3.设置用户状态 1:正常 0:禁用
        sysUser.setStatus(1);
        sysUserMapper.save(sysUser);
    }

    @Override
    public void updateSysUser(SysUser sysUser) {
        //1.判断用户名不能重复
        String userName = sysUser.getUserName();
        SysUser dbSysUser = sysUserMapper.selectUserInfoByUsername(userName);
        if (dbSysUser != null) {
            throw new GuiguException(ResultCodeEnum.USER_NAME_IS_EXISTS);
        }
        sysUserMapper.update(sysUser);
    }
    //删除用户
    @Override
    public void deleteSysUser(Long userId) {
        sysUserMapper.delete(userId);

    }
    //分配角色
    @Override
    public void doAssign(AssginRoleDto assginRoleDto) {
        //根据userId删除用户之前分配的角色数据
        sysRoleUserMapper.deleteByUserId(assginRoleDto.getUserId());

        //重新分配新数据
        List<Long> roleIdList = assginRoleDto.getRoleIdList();

        //遍历得到每个角色id
        for (Long roleId : roleIdList) {
            sysRoleUserMapper.doAssign(assginRoleDto.getUserId(),roleId);
        }

    }
}
