package com.example.service;

import com.example.entity.Account;
import com.example.entity.LeaveRequest;
import com.example.exception.CustomerException;
import com.example.mapper.LeaveRequestMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假申请业务逻辑层
 * 处理请假申请的增删改查、审核操作、分页查询等核心业务逻辑
 */
@Service
public class LeaveRequestService {

    /** 请假申请数据访问对象，用于数据库交互 */
    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    /**
     * 分页查询请假申请列表
     * 支持按标题模糊查询，按请求人ID、审核人ID、状态进行筛选
     * 查询结果按提交时间倒序排列（最新的在前）
     * @param pageNum  当前页码（从1开始）
     * @param pageSize 每页显示记录数
     * @param leaveRequest 查询条件封装对象（可包含title/requesterId/reviewerId/status）
     * @return 分页结果对象，包含总记录数和当前页数据列表
     */
    public PageInfo<LeaveRequest> selectPage(Integer pageNum, Integer pageSize, LeaveRequest leaveRequest){
        // 开启分页查询插件，自动处理SQL的LIMIT语句
        PageHelper.startPage(pageNum, pageSize);
        List<LeaveRequest> list = leaveRequestMapper.selectList(leaveRequest);
        // 将查询结果包装成分页对象返回
        return PageInfo.of(list);

    }

    /**
     * 新增请假申请
     * 自动设置提交时间戳和初始状态为"待审核"
     * 调用前需确保requesterId和reviewerId已正确设置
     * @param leaveRequest 请假申请实体对象（必须包含title、description、requesterId、reviewerId）
     */
    public void add(LeaveRequest leaveRequest){
        // 设置初始状态为"待审核"
        leaveRequest.setStatus("待审核");
        // 记录当前系统时间作为提交时间
        leaveRequest.setSubmitTime(LocalDateTime.now());
        // 执行数据库插入操作
        leaveRequestMapper.insert(leaveRequest);

    }

    /**
     * 更新请假申请信息
     * 仅允许在未审核状态下修改基本信息（标题、说明等）
     * 审核相关的字段（状态、审核说明）应通过review方法更新
     * @param leaveRequest 包含id和需要更新字段的实体对象
     */
    public void update(LeaveRequest leaveRequest) {
        leaveRequestMapper.update(leaveRequest);
    }

    /**
     * 删除请假申请记录
     * 仅允许在未审核状态下删除（业务层面控制，此处仅执行物理删除）
     * @param id 请假申请的主键ID
     */
    public void deleteById(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        LeaveRequest request = leaveRequestMapper.selectById(id);

        // 只有申请人本人或管理员可以删除
        if (!currentUser.getId().equals(request.getRequesterId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new CustomerException("无权删除此申请");
        }

        leaveRequestMapper.deleteById(id);
    }

    /**
     * 审核请假申请
     * 更新审核状态、审核说明和审批时间
     * 调用前需校验：拒绝时必须填写审核说明
     * @param leaveRequest 包含id、status（已审核/审核拒绝）、reviewComment（拒绝时必填）的对象
     */
    public void review(LeaveRequest leaveRequest) {

        leaveRequest.setReviewTime(LocalDateTime.now());
        leaveRequestMapper.update(leaveRequest);
    }

    /**
     * 根据主键ID查询请假申请详情
     * 用于编辑前加载数据或查看详情场景
     * @param id 请假申请的主键ID
     * @return 请假申请完整对象，不存在时返回null
     */
    public LeaveRequest selectById(Integer id) {
        // 构建查询条件
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(id);
        // 执行查询并返回第一条（应该只有一条）结果
        List<LeaveRequest> list = leaveRequestMapper.selectList(leaveRequest);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 获取月度请假统计数据
     * 统计当前年份1-6月每月的请假数量
     * @param requesterId 申请人ID（可选，用于区分用户角色）
     * @return 月度统计数据列表
     */
    public List<Map<String, Object>> getMonthlyStats(Integer requesterId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取当前年份
        int currentYear = LocalDateTime.now().getYear();

        // 统计1-6月的数据
        for (int month = 1; month <= 6; month++) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month + "月");
            // 查询该月的请假数量
            int count = leaveRequestMapper.countByMonth(currentYear, month, requesterId);
            monthData.put("count", count);

            result.add(monthData);
        }
        return result;
    }
}

