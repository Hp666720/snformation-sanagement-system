package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /** 条件查询用户列表（支持模糊搜索） */
    List<User> getListUser(User user);

    /** 新增用户 */
    int addUser(User user);

    /** 更新用户信息 */
    int update(User user);

    /** 根据ID删除用户 */
    int deleteUserById(int id);

    /** 根据用户名查询（用于登录校验） */
    User selectUserByName(String username);

    /** 根据ID查询用户详情 */
    User selectUserById(Integer id);
}
