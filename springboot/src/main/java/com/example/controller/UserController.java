package com.example.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;

import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomerException;
import com.example.service.AdminService;
import com.example.service.UserService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户相关的CRUD和登录注册操作
 * 权限要求：增删改操作仅限ADMIN角色，查询操作ADMIN可见全部、USER仅见自己
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    /**
     * 分页查询用户列表
     * 支持按username、name等字段模糊筛选，返回分页结果
     * ADMIN角色可查看所有用户，USER角色只能查看自己的信息
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @param user 查询条件
     * @return 分页结果
     */
    @RequestMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             User user) {
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            // 强制限制：普通用户只能查看自己的数据
            user.setId(currentUser.getId());
        }
        log.info("用户分页查询请求: pageNum={}, pageSize={}, 查询条件={}", pageNum, pageSize, user);
        PageInfo<User> pageInfo = userService.selectPage(pageNum, pageSize, user);
        log.info("用户分页查询结果: 总记录数={}, 当前页记录数={}", pageInfo.getTotal(), pageInfo.getList().size());
        return Result.success(pageInfo);
    }

    /**
     * 新增用户
     * 校验用户名唯一性，未提供密码时默认设为123456，角色固定为USER
     * @param user 用户信息（username必填）
     * @return 新增的用户对象
     */
    @RequestMapping("/addUser")
    public Result addUser(@RequestBody User user){
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限新增用户");
        }
        log.info("新增用户请求: username={}, name={}", user.getUsername(), user.getName());
        userService.addUser(user);
        log.info("新增用户成功: username={}, id={}", user.getUsername(), user.getId());
        return Result.success("200", "新增成功",user);
    }

    /**
     * 更新用户信息
     * 根据id更新非空字段
     * USER角色只能修改自己的信息，ADMIN可修改任意用户
     * @param user 包含id的更新数据
     * @return 更新后的用户对象
     */
    @RequestMapping("/update")
    public Result update(@RequestBody User user){
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole()) && !currentUser.getId().equals(user.getId())){
            throw new CustomerException("无权限修改他人信息");
        }
        log.info("更新用户信息请求: id={}", user.getId());
        userService.update(user);
        log.info("更新用户信息成功: id={}", user.getId());
        return Result.success("200", "更新成功",user);
    }

    /**
     * 根据ID删除单个用户
     * 物理删除，不可恢复
     * @param id 用户主键ID
     * @return 删除的ID
     */
    @RequestMapping("/deleteUserById/{id}")
    public Result deleteUserById(@PathVariable int id){
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限删除用户");
        }
        log.info("删除用户请求: id={}", id);
        userService.deleteUserById(id);
        log.info("删除用户成功: id={}", id);
        return Result.success("200", "删除成功",id);
    }

    /**
     * 批量删除用户（内部方法）
     * @param ids 待删除的用户ID数组
     * @return 删除的ID数组
     */
    @RequestMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Integer[] ids) {
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限批量删除用户");
        }
        userService.deleteBatchByIds(ids);
        return Result.success("200", "删除成功", ids);
    }

    /**
     * 用户登录
     * 校验角色必须是USER，验证账号密码后返回用户信息
     * @param account 登录凭证（username、password、role=USER必填）
     * @return 登录成功的用户对象（含token）
     */
    @RequestMapping("/login")
    public Result login(@RequestBody Account account) {
        if (!"USER".equals(account.getRole())) {
            throw new CustomerException("非法请求：用户登录接口仅支持普通用户角色");
        }
        log.info("用户登录请求: username={}", account.getUsername());
        User user = userService.login(account);
        log.info("用户登录成功: username={}, id={}", user.getUsername(), user.getId());
        if(user == null){
            return Result.error("用户名或密码错误");
        }
        return Result.success(user);
    }

    /**
     * 用户注册
     * 调用addUser完成注册，默认密码123456、角色USER
     * @param user 注册用户信息（username必填）
     * @return 注册的用户对象
     */
    @RequestMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success("200", "注册成功",user);
    }

    /**
     * 检查用户用户名是否存在
     * 用于前端实时校验用户名唯一性
     * @param username 待检查的用户名
     * @return {exists: true/false}
     */
    @GetMapping("/checkUsername")
    public Result checkUsername(@RequestParam String username) {
        log.info("检查用户用户名: username={}", username);
        User existUser = userService.selectUserByName(username);
        boolean exists = existUser != null;
        HashMap<String, Object> data = new HashMap<>();
        data.put("exists", exists);
        return Result.success("200", "查询成功", data);
    }

    /**
     * 导出用户数据为Excel文件
     * 导出字段：账号、名称、电话、邮箱
     * @param user 查询条件
     * @param response HTTP响应对象
     */
    @RequestMapping("/exportUser")
    public void exportUser(User user, HttpServletResponse response) throws Exception {
        // 处理ids参数为空的情况，避免SQL语法错误
        if (user.getIds() != null && user.getIds().length == 1 && user.getIds()[0] == null) {
            user.setIds(null);
        }
        log.info("导出用户数据请求: 查询条件={}", user);
        //1:根据参数，查询要导出的列表
        List<User> list = userService.selectUserList(user);
        // 2:创建一个Writer对象
        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 设置中文表头映射
        writer.addHeaderAlias("username", "账号");
        writer.addHeaderAlias("name", "名称");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("email", "邮箱");
        // 4: 设置只添加了中文的表头
        writer.setOnlyAlias(true);
        // 5：写入数据
        writer.write(list, true);
        // 6:设置输出文件名称以及输出流的头信息
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        String fileName = URLEncoder.encode("用户列表", String.valueOf(StandardCharsets.UTF_8));
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        // 7:写出到输出流，关闭
        ServletOutputStream os = response.getOutputStream();
        writer.flush(os);
        os.close();
        writer.close();
    }

    /**
     * 从Excel导入用户数据
     * 解析上传的Excel并批量创建用户，默认密码123456，角色USER
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/importUser")
    public Result importUser(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要导入的文件");
        }

        log.info("开始导入用户Excel文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            //1:拿到输入流
            InputStream is = file.getInputStream();
            //2:创建一个ExcelReader对象
            ExcelReader reader = ExcelUtil.getReader(is);
            //3:通过reader对象，读取数据
            reader.addHeaderAlias("账号", "username");
            reader.addHeaderAlias("名称", "name");
            reader.addHeaderAlias("电话", "phone");
            reader.addHeaderAlias("邮箱", "email");

            List<User> userList = reader.readAll(User.class);

            if (userList == null || userList.isEmpty()) {
                return Result.error("Excel文件中没有数据");
            }

            log.info("读取到{}条用户数据，开始批量导入", userList.size());

            int successCount = 0;
            int failCount = 0;
            StringBuilder failMsg = new StringBuilder();

            for (User user : userList) {
                try {
                    user.setRole("USER");
                    userService.addUser(user);
                    successCount++;
                    log.info("成功导入用户: {}", user.getUsername());
                } catch (Exception e) {
                    failCount++;
                    failMsg.append(user.getUsername()).append("导入失败: ").append(e.getMessage()).append("; ");
                    log.error("导入用户{}失败", user.getUsername(), e);
                }
            }

            log.info("导入完成，成功{}条，失败{}条", successCount, failCount);

            if (successCount > 0 && failCount == 0) {
                return Result.success("导入成功，共导入" + successCount + "条数据");
            } else if (successCount > 0 && failCount > 0) {
                return Result.success("导入完成，成功" + successCount + "条，失败" + failCount + "条");
            } else {
                return Result.error("导入失败: " + failMsg.toString());
            }
        } catch (Exception e) {
            log.error("导入Excel文件失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }



}


