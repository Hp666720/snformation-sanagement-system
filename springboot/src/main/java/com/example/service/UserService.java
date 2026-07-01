package com.example.service;

import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomerException;

import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * 用户业务逻辑层
 * 处理用户增删改查、登录认证、注册等核心业务
 */
@Service
public class UserService {

    /** 用户数据访问对象 */
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取所有用户列表（不分页）
     * @return 全部用户列表，无数据时返回空集合
     */
    public List<User> getListUser(){
        return userMapper.getListUser(null);
    }

    /**
     * 查询用户列表（不分页，带查询条件）
     * 用于导出功能，根据条件查询用户数据
     * @param user 查询条件
     * @return 匹配条件的用户列表
     */
    public List<User> selectUserList(User user) {
        return userMapper.getListUser(user);
    }

    /**
     * 分页查询用户列表
     * 支持按username、name等条件模糊搜索
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param user     查询条件（可为null）
     * @return 分页结果对象
     */
    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize, User user) {
        // 开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<User> userList = userMapper.getListUser(user);
        return PageInfo.of(userList);
    }

    /**
     * 新增用户
     * 自动设置默认密码和角色，校验用户名唯一性
     * @param user 用户信息（username必填，password/name选填有默认值）
     * @return 受影响行数
     * @throws CustomerException 账号已被占用时抛出
     */
    public int addUser(User user){
        // 检查账号唯一性（只根据用户名查询）
        User existUser = userMapper.selectUserByName(user.getUsername());
        if (existUser != null){
            throw new CustomerException("该账号已被占用，请更换新的账号");
        }
        // 设置默认值
        if (Strings.isBlank(user.getPassword())) {
            user.setPassword("123456");
        }
        if (Strings.isBlank(user.getName())) {
            user.setName(user.getUsername());
        }
        user.setRole("USER");
        return userMapper.addUser(user);
    }

    /**
     * 更新用户信息
     * @param user 包含id的更新数据
     * @return 受影响行数
     */
    public int update(User user){
        return userMapper.update(user);
    }

    /**
     * 根据ID删除单个用户
     * @param id 用户主键ID
     * @return 受影响行数
     */
    public int deleteUserById(int id){
        return userMapper.deleteUserById(id);
    }

    /**
     * 批量删除用户（通过用户对象列表）
     * @param list 待删除的用户列表
     */
    public void deleteBatch(List<User> list) {
        for (User user : list) {
            this.deleteUserById(user.getId());
        }
    }

    /**
     * 根据ID数组批量删除用户
     * @param ids 待删除的用户ID数组
     */
    public void deleteBatchByIds(Integer[] ids) {
        for (Integer id : ids) {
            this.deleteUserById(id);
        }
    }

    /**
     * 用户登录认证
     * 验证账号密码并生成JWT Token
     * @param account 登录凭证（username、password必填）
     * @return 登录成功的用户对象（含token）
     * @throws CustomerException 账号不存在或密码错误时抛出
     */
    public User login(Account account) {
        // 验证账号是否存在
        User dbUser = userMapper.selectUserByName(account.getUsername());
        if (dbUser == null) {
            throw new CustomerException("账号不存在");
        }
        // 验证密码是否正确
        if (!dbUser.getPassword().equals(account.getPassword())) {
            throw new CustomerException("账号或密码错误");
        }

        // 创建token并返回给前端
        String token = TokenUtils.createToken(String.valueOf(dbUser.getId()), "USER", dbUser.getPassword());
        dbUser.setToken(token);

        return dbUser;
    }

    /**
     * 用户注册入口方法
     * 内部委托给addUser执行实际注册逻辑
     * @param user 注册用户信息
     * @throws CustomerException 注册失败时抛出
     */
    public void register(User user) {
        this.addUser(user);
    }

    /**
     * 根据用户ID查询详情
     * @param id 用户主键ID
     * @return 用户对象，不存在则返回null
     */
    public User selectById(Integer id) {
        return userMapper.selectUserById(id);
    }

    /**
     * 根据用户名查询用户
     * 用于检查用户名唯一性
     * @param username 用户名
     * @return 用户对象，不存在则返回null
     */
    public User selectUserByName(String username) {
        return userMapper.selectUserByName(username);
    }


}
