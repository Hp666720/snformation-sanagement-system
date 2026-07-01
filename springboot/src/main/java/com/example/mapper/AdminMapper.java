package com.example.mapper;

import com.example.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员数据访问接口
 */
@Mapper
public interface AdminMapper {

    /*
    * 通用查询接口
     */
    List<Admin> selectAdminList(Admin admin);

    /*
    * 精确匹配查询管理员
    * */
    Admin selectAdmin(Admin admin);

    /** 新增管理员 */
    int insertAdmin(Admin admin);

    /** 更新管理员信息 */
    int updateAdmin(Admin admin);

    /** 根据ID删除管理员 */
    int deleteAdmin(Integer id);

    /** 通用列表查询（分页场景使用） */
    List<Admin> getListAdmin(Admin admin);

    /** 批量删除（使用IN语句） */
    int deleteBatch(@Param("ids") Integer[] ids);

    /** 根据ID查询管理员详情 */
    Admin getById(Integer id);

    /** 根据用户名查询（用于登录校验） */
    Admin selectAdminByName(String username);

    /** 根据ID查询管理员（别名方法） */
    Admin selectAdminById(Integer id);
}
