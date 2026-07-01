package com.example.service;

import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomerException;
import com.example.mapper.AdminMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员业务逻辑层
 * 处理管理员增删改查、登录认证、分页查询（含权限过滤）等业务
 */
@Service
public class AdminService {

    /** 管理员数据访问对象 */
    @Autowired
    private AdminMapper adminMapper;


    /**
     * 分页查询管理员列表（带权限过滤）
     * 管理员可查看所有数据，普通用户只能查看自己的信息
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param admin    查询条件（role字段决定查询目标类型：ADMIN/USER）
     * @return 分页结果对象
     */
    public PageInfo<Admin> selectAdminPage(Integer pageNum,
                                           Integer pageSize, Admin admin){
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> adminList = adminMapper.selectAdminList(admin);
        return PageInfo.of(adminList);
    }

    /**
     * 管理员登录（通过Admin对象）
     * @param admin 包含username和password的管理员对象
     * @return 登录成功的管理员对象（含token）
     * @throws CustomerException 登录失败时抛出
     */
    public Admin login(Admin admin) {
        Account account = new Account();
        account.setUsername(admin.getUsername());
        account.setPassword(admin.getPassword());
        account.setRole("ADMIN");
        return login(account);
    }

    /**
     * 添加新管理员
     * 未提供密码时默认设置为"123456"
     * @param admin 管理员信息
     * @return 受影响行数
     */
    public int addAdmin(Admin admin) {
        // 检查账号唯一性（只根据用户名查询）
        Admin existAdmin = adminMapper.selectAdminByName(admin.getUsername());
       if (existAdmin != null){
           throw new CustomerException("该账号已被占用，请更换新的账号");
       }
        if (admin.getPassword() == null){
            admin.setPassword("123456");
        }
        return adminMapper.insertAdmin(admin);
    }
    /**
     * 更新管理员信息
     * @param admin 包含id的更新数据
     * @return 受影响行数
     */
    public int updateAdmin(Admin admin) {
        return adminMapper.updateAdmin(admin);
    }

    /**
     * 删除管理员
     * @param id 管理员主键ID
     * @return 受影响行数
     */
    public int deleteAdmin(Integer id) {
        return adminMapper.deleteAdmin(id);
    }

    /**
     * 批量删除管理员
     * @param ids 待删除的管理员ID数组
     * @return 成功删除的数量
     */
    public int deleteBatch(Integer[] ids) {
        return adminMapper.deleteBatch(ids);
    }

    /**
     * 查询管理员列表（不分页）
     * @param admin 查询条件
     * @return 匹配条件的管理员列表
     */
    public List<Admin> selectAdminList(Admin admin) {
        return adminMapper.selectAdminList(admin);
    }

    /**
     * 管理员登录认证（核心实现）
     * 验证账号密码并生成JWT Token
     * @param account 登录凭证（username、password必填，role应为"ADMIN"）
     * @return 认证成功的管理员对象（含token）
     * @throws CustomerException 账号不存在或密码错误时抛出
     */
    public Admin login(Account account) {
        // 验证账号是否存在
        Admin dbAdmin = adminMapper.selectAdminByName(account.getUsername());
        if (dbAdmin == null) {
            throw new CustomerException("账号不存在");
        }
        // 验证密码是否正确
        if (!dbAdmin.getPassword().equals(account.getPassword())) {
            throw new CustomerException("账号或密码错误");
        }
        //创建token并返回给前端
        String token = TokenUtils.createToken(String.valueOf(dbAdmin.getId()), "ADMIN", dbAdmin.getPassword());
        dbAdmin.setToken(token);

        return dbAdmin;
    }

    /**
     * 根据ID查询管理员详情
     * @param id 管理员主键ID
     * @return 管理员对象，不存在则返回null
     */
   public Admin getById(Integer id) {
        return adminMapper.getById(id);
    }

    /**
     * 根据用户名查询管理员
     * 用于检查用户名唯一性
     * @param username 用户名
     * @return 管理员对象，不存在则返回null
     */
    public Admin selectAdminByName(String username) {
        return adminMapper.selectAdminByName(username);
    }


}
