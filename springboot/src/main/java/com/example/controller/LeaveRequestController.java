package com.example.controller;

import com.example.common.Result;
import com.example.entity.LeaveRequest;
import com.example.exception.CustomerException;
import com.example.service.LeaveRequestService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 请假申请管理控制器
 * 处理请假申请的CRUD操作、审核流程、权限控制等HTTP请求
 * 权限说明：
 *   - 普通用户（USER）：可提交、查看、编辑、删除自己的申请
 *   - 管理员（ADMIN）：可查看分配给自己的申请并进行审核
 */
@RestController
@RequestMapping("/leave")
@Slf4j
public class LeaveRequestController {

    /** 请假申请业务逻辑层对象 */
    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * 分页查询请假申请列表
     * 支持按标题模糊查询；前端需根据角色传入不同的查询条件：
     *   - 普通用户：传入requesterId=当前用户ID，查询自己提交的申请
     *   - 管理员：传入reviewerId=当前管理员ID，查询需要自己审核的申请
     * @param pageNum  当前页码（默认第1页）
     * @param pageSize 每页显示数量（默认10条）
     * @param leaveRequest 查询条件对象（title支持模糊匹配）
     * @return 包含分页信息的Result对象（code=200表示成功）
     */
    @RequestMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             LeaveRequest leaveRequest){
        log.info("请假申请分页查询请求: pageNum={}, pageSize={}, 查询条件={}", pageNum, pageSize, leaveRequest);
        PageInfo<LeaveRequest> pageInfo = leaveRequestService.selectPage(pageNum, pageSize, leaveRequest);
        log.info("请假申请分页查询结果: 总记录数={}, 当前页记录数={}", pageInfo.getTotal(), pageInfo.getList().size());
        return Result.success(pageInfo);
    }

    /**
     * 新增请假申请
     * 仅普通用户可调用此接口；自动记录请求人信息并设置初始状态为"待审核"
     * @param leaveRequest 请假申请对象（必须包含：title标题、description说明、reviewerId审核人ID）
     * @return 操作结果（成功时返回新建的申请对象，包含数据库生成的ID）
     * @throws CustomerException 当标题为空时抛出异常
     */
    @RequestMapping("/add")
    public Result add(@RequestBody LeaveRequest leaveRequest){
        // 校验必填字段：请假标题不能为空
        if(leaveRequest.getTitle() == null || leaveRequest.getTitle().isEmpty()){
            throw new CustomerException("标题不能为空");
        }
        log.info("新增请假申请请求: title={}, requesterId={}, reviewerId={}",
                leaveRequest.getTitle(), leaveRequest.getRequesterId(), leaveRequest.getReviewerId());
        // 执行新增操作
        leaveRequestService.add(leaveRequest);
        log.info("新增请假申请成功: id={}, title={}", leaveRequest.getId(), leaveRequest.getTitle());
        return Result.success("200","申请提交成功",leaveRequest);
    }

    /**
     * 更新请假申请信息
     * 业务限制：仅允许在"待审核"状态下修改，已审核或已拒绝的申请不可修改
     * 前端调用前应先检查状态，避免不必要的请求
     * @param leaveRequest 包含id和更新字段的实体对象（通常只更新title和description）
     * @return 操作结果
     * @throws CustomerException 当申请不存在或状态不允许修改时抛出异常
     */
    @RequestMapping("/update")
    public Result update(@RequestBody LeaveRequest leaveRequest){
        log.info("更新请假申请请求: id={}", leaveRequest.getId());
        // 先查询原始数据以校验状态
        LeaveRequest existing = leaveRequestService.selectById(leaveRequest.getId());
        if(existing == null){
            throw new CustomerException("申请不存在");
        }
        if(!"待审核".equals(existing.getStatus())){
            throw new CustomerException("状态不允许修改");
        }
        // 执行更新操作
        leaveRequestService.update(leaveRequest);
        log.info("更新请假申请成功: id={}", leaveRequest.getId());
        return Result.success("200","申请更新成功",leaveRequest);
    }

    /**
     * 删除请假申请
     * 业务限制：仅允许在"待审核"状态下删除，已审核或已拒绝的申请不可删除
     * 删除操作不可恢复，前端应提供二次确认提示
     * @param id 请假申请的主键ID
     * @return 操作结果
     * @throws CustomerException 当申请不存在或状态不允许删除时抛出异常
     */
    @RequestMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        log.info("删除请假申请请求: id={}", id);
        // 先查询原始数据以校验状态
        LeaveRequest existing = leaveRequestService.selectById(id);
        if(existing == null){
            throw new CustomerException("申请不存在");
        }
        if(!"待审核".equals(existing.getStatus())){
            throw new CustomerException("状态不允许删除");
        }
        leaveRequestService.deleteById(id);
        log.info("删除请假申请成功: id={}", id);
        return Result.success("200","删除成功",id);
    }

    /**
     * 审核请假申请
     * 仅管理员可调用此接口；根据审核结果设置不同状态：
     *   - 已审核：表示通过，无需填写审核说明
     *   - 审核拒绝：表示不通过，必须填写拒绝原因
     * 审核后系统自动记录审批时间
     * @param leaveRequest 包含以下字段的对象：
     *                     - id: 申请ID（必填）
     *                     - status: 审核结果（必填，值为"已审核"或"审核拒绝"）
     *                     - reviewComment: 审核说明（拒绝时必填）
     * @return 操作结果
     * @throws CustomerException 当状态值非法或拒绝时未填写说明时抛出异常
     */
    @RequestMapping("/review")
    public Result review(@RequestBody LeaveRequest leaveRequest){
        log.info("审核请假申请请求: id={}, status={}", leaveRequest.getId(), leaveRequest.getStatus());
        //校验审核状态的合法性
        if(leaveRequest.getStatus() == null ||
                (!"已审核".equals(leaveRequest.getStatus())
                        && !"审核拒绝".equals(leaveRequest.getStatus()))){
            throw new CustomerException("请选择正确的审核状态");

        }
        //校验拒绝时的必填项：审核说明
        if("审核拒绝".equals(leaveRequest.getStatus())&&
                (leaveRequest.getReviewComment() == null ||
                        leaveRequest.getReviewComment().trim().isEmpty())){
            throw new CustomerException("请填写拒绝原因");
        }
        // 调用Service层执行审核逻辑（会自动设置审核时间）
        leaveRequestService.review(leaveRequest);
        log.info("审核请假申请成功: id={}, status={}", leaveRequest.getId(), leaveRequest.getStatus());
        return Result.success("200","审核成功",leaveRequest);
    }

    /**
     * 根据ID查询请假申请详情
     * 用于编辑前加载数据或查看完整信息
     *
     * @param id 请假申请的主键ID
     * @return 包含完整申请信息的Result对象
     */
    @RequestMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        log.info("查询请假申请详情请求: id={}", id);
        LeaveRequest leaveRequest = leaveRequestService.selectById(id);
        return Result.success(leaveRequest);
    }

    /**
     * 获取月度请假统计数据
     * 返回当前年份1-6月的请假数量统计
     */
    @GetMapping("/getMonthlyStats")
    public Result getMonthlyStats(@RequestParam(required = false) Integer requesterId) {
        log.info("获取月度请假统计请求: requesterId={}", requesterId);
        List<Map<String, Object>> monthlyStats = leaveRequestService.getMonthlyStats(requesterId);
        log.info("获取月度请假统计成功: count={}", monthlyStats.size());
        return Result.success(monthlyStats);
    }
}
