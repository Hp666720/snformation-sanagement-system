package com.example.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomerException;
import com.example.service.AdminService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 * 管理员管理控制器
 * 处理管理员账户的CRUD、分页查询、Excel导入导出操作
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 分页查询管理员列表（带权限过滤）
     * ADMIN角色可查看全部数据，USER角色只能查看自己的信息
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认5）
     * @param admin 查询条件
     * @return 分页结果
     */
    @RequestMapping("/selectAdminPage")
    public Result selectAdminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "5") Integer pageSize,
                                  Admin admin){
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限查看管理员信息");
        }
        log.info("管理员分页查询请求: pageNum={}, pageSize={}, 查询条件={}", pageNum, pageSize, admin);
        PageInfo<Admin> pageInfo = adminService.selectAdminPage(pageNum, pageSize, admin);
        log.debug("管理员分页查询结果: 总记录数={}, 当前页记录数={}", pageInfo.getTotal(), pageInfo.getList().size());
        return Result.success(pageInfo);
    }

    /**
     * 管理员登录
     * 验证账号密码后返回管理员信息及token
     * @param admin 登录凭证（username、password必填）
     * @return 登录成功的管理员对象（含token）
     */
    @RequestMapping("/login")
    public Result login(@RequestBody Admin admin) {
        log.info("管理员登录请求: username={}", admin.getUsername());
        Admin result = adminService.login(admin);
        if (result != null) {
            return Result.success(result);
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 新增管理员
     * 未提供密码时默认设为123456
     * @param admin 管理员信息（username必填）
     * @return 新增的管理员对象
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Admin admin) {
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限添加管理员");
        }
        log.info("新增管理员请求: username={}, name={}", admin.getUsername(), admin.getName());
        adminService.addAdmin(admin);
        log.info("新增管理员成功: username={}, id={}", admin.getUsername(), admin.getId());
        return Result.success("200", "新增成功", admin);
    }

    /**
     * 更新管理员信息
     * 根据id更新非空字段
     * @param admin 包含id的更新数据
     * @return 更新后的管理员对象
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Admin admin) {
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限修改管理员信息");
        }
        log.info("更新管理员信息请求: id={}", admin.getId());
        adminService.updateAdmin(admin);
        log.info("更新管理员信息成功: id={}", admin.getId());
        return Result.success("200", "更新成功", admin);
    }

    /**
     * 删除单个管理员（物理删除）
     * @param id 管理员主键ID
     * @return 删除的ID
     */
    @RequestMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限删除管理员");
        }
        log.info("删除管理员请求: id={}", id);
        adminService.deleteAdmin(id);
        log.info("删除管理员成功: id={}", id);
        return Result.success("200", "删除成功", id);
    }

    /**
     * 获取管理员列表（用于请假申请选择审批人）
     * 允许所有登录用户访问，仅返回管理员的基本信息（id、name、username）
     * @param name 姓名模糊查询条件（可选）
     * @return 管理员列表
     */
    @RequestMapping("/getAdminList")
    public Result getAdminList(@RequestParam(defaultValue = "") String name) {
        log.info("获取管理员列表请求: name={}", name);
        Admin admin = new Admin();
        if (name != null && !name.trim().isEmpty()) {
            admin.setName(name);
        }
        // 查询所有管理员，不分页
        List<Admin> list = adminService.selectAdminList(admin);
        log.info("获取管理员列表成功: count={}", list.size());
        return Result.success(list);
    }

    /**
     * 检查管理员用户名是否存在
     * 用于前端实时校验用户名唯一性
     * @param username 待检查的用户名
     * @return {exists: true/false}
     */
    @GetMapping("/checkUsername")
    public Result checkUsername(@RequestParam String username) {
        log.info("检查管理员用户名: username={}", username);
        Admin existAdmin = adminService.selectAdminByName(username);
        boolean exists = existAdmin != null;
        HashMap<String, Object> data = new HashMap<>();
        data.put("exists", exists);
        return Result.success("200", "查询成功", data);
    }

    /**
     * 批量删除管理员 - 仅ADMIN角色可调用
     * @param ids 待删除的管理员ID数组
     * @return 删除的ID数组
     */
    @RequestMapping("/deleteBatch/{ids}")
    public Result deleteBatch(@PathVariable String ids) {
        Account currentUser = TokenUtils.getCurrentUser();
        if("USER".equals(currentUser.getRole())){
            throw new CustomerException("无权限删除管理员");
        }
        String[] idArray = ids.split(",");
        Integer[] intIds = new Integer[idArray.length];
        for (int i = 0; i < idArray.length; i++) {
            intIds[i] = Integer.parseInt(idArray[i]);
        }
        adminService.deleteBatch(intIds);
        return Result.success("200", "批量删除成功", intIds);
    }

    /**
     * 导出管理员数据为Excel文件
     * 导出字段：用户名、姓名、电话、邮箱
     * @param admin 查询条件
     * @param response HTTP响应对象
     */
    @RequestMapping("/exportAdmin")
    public void export(Admin admin , HttpServletResponse response) throws Exception{
        // 处理ids参数为空的情况，避免SQL语法错误
          if (admin.getIds() != null && admin.getIds().length == 1 && admin.getIds()[0] == null) {
                   admin.setIds(null);
              }
        log.info("导出管理员数据请求: 查询条件={}", admin);
        //1:根据参数，查询要导出的列表
        List<Admin> list = adminService.selectAdminList(admin);
        // 2:创建一个Writer对象
        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 设置中文表头映射
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("name", "姓名");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("email", "邮箱");
        // 4: 设置只添加了中文的表头
        writer.setOnlyAlias( true);
        // 5：写入数据
        writer.write(list, true);
        // 6:设置输出文件名称以及输出流的头信息
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        String fileName = URLEncoder.encode("用户列表",String.valueOf( StandardCharsets.UTF_8));
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        // 7:写出到输出流，关闭
        ServletOutputStream os = response.getOutputStream();
        writer.flush(os);
        os.close();
        writer.close();
        log.info("导出管理员数据成功: count={}", list.size());
    }

    /**
     * 从Excel导入管理员数据
     * 解析上传的Excel并批量创建管理员，默认密码123456
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/importAdmin")
    public Result importData(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要导入的文件");
        }

        log.info("开始导入Excel文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            //1:拿到输入流
            InputStream is = file.getInputStream();
            //2:创建一个ExcelReader对象
            ExcelReader reader = ExcelUtil.getReader(is);
            //3:通过reader对象，读取数据
            reader.addHeaderAlias("用户名", "username");
            reader.addHeaderAlias("姓名", "name");
            reader.addHeaderAlias("电话", "phone");
            reader.addHeaderAlias("邮箱", "email");

            List<Admin> adminList = reader.readAll(Admin.class);

            if (adminList == null || adminList.isEmpty()) {
                return Result.error("Excel文件中没有数据");
            }

            log.info("读取到{}条管理员数据，开始批量导入", adminList.size());

            int successCount = 0;
            int failCount = 0;
            StringBuilder failMsg = new StringBuilder();

            for (Admin admin : adminList) {
                try {
                    admin.setRole("ADMIN");
                    adminService.addAdmin(admin);
                    successCount++;
                    log.info("成功导入管理员: {}", admin.getUsername());
                } catch (Exception e) {
                    failCount++;
                    failMsg.append(admin.getUsername()).append("导入失败: ").append(e.getMessage()).append("; ");
                    log.error("导入管理员{}失败", admin.getUsername(), e);
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
