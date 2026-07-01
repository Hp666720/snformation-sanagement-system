package com.example.mapper;


import com.example.entity.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 请假申请数据访问层接口
 * 定义对leave_request表的CRUD操作
 */
@Mapper
public interface LeaveRequestMapper {


    /**
     * 查询请假申请列表（支持多条件筛选）
     * @param leaveRequest 查询条件对象，支持按标题模糊查询、请求人ID、审核人ID、状态筛选
     * @return 匹条件的请假申请列表，按提交时间倒序排列
     */
    List<LeaveRequest> selectList(@Param("leaveRequest") LeaveRequest leaveRequest);


    /**
     * 新增请假申请记录
     * @param leaveRequest 请假申请实体对象（包含标题、说明、请求人、审核人等信息）
     * @return 受影响的行数（成功返回1）
     */
    int insert(LeaveRequest leaveRequest);

    /**
     * 更新请假申请信息
     * 支持部分字段更新，仅更新非空字段
     * @param leaveRequest 包含id的更新数据对象（可包含状态、审核说明等）
     * @return 受影响的行数（成功返回1）
     */
    int update(LeaveRequest leaveRequest);

    /**
     * 根据主键ID删除请假申请记录
     * @param id 请假申请主键ID
     * @return 受影响的行数（成功返回1）
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 统计指定年月的请假数量
     *
     * @param year 年份
     * @param month 月份
     * @param requesterId 申请人ID（可选）
     * @return 请假数量
     */
    int countByMonth(@Param("year") int year, @Param("month") int month,  @Param("requesterId") Integer requesterId);

    /**
     * 根据主键ID查询请假申请详情
     * @param id 请假申请主键ID
     * @return 请假申请完整对象，不存在时返回null
     */
    LeaveRequest selectById(Integer id);

}
