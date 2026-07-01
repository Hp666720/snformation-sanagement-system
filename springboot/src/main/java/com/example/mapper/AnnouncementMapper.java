package com.example.mapper;


import com.example.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告数据访问层接口
 * 定义对announcement表的CRUD操作
 */
@Mapper
public interface AnnouncementMapper {

    /**
     * 查询公告列表（支持多条件筛选）
     * @param announcement 查询条件对象，支持按标题模糊查询、发布人ID、状态筛选
     * @return 匹配条件的公告列表，按发布时间倒序排列
     */
    List<Announcement> selectList(@Param("announcement") Announcement announcement);


    /**
     * 新增公告记录
     * @param announcement 公告实体对象（包含标题、内容、发布人等信息）
     * @return 受影响的行数（成功返回1）
     */
    int insert(Announcement announcement);

    /**
     * 更新公告信息
     * 支持部分字段更新，仅更新非空字段
     * @param announcement 包含id的更新数据对象（可包含标题、内容、状态等）
     * @return 受影响的行数（成功返回1）
     */
    int update(Announcement announcement);

    /**
     * 根据id删除公告记录
     * @param id 要删除的记录id
     * @return 受影响的行数（成功返回1）
     */
    int deleteById(@Param("id") Integer id);
}
