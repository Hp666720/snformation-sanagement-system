package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Announcement;
import com.example.exception.CustomerException;
import com.example.mapper.AnnouncementMapper;
import com.example.service.AnnouncementService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理控制器
 * 处理公告的CRUD操作、发布/撤回切换、首页数据获取等HTTP请求
 * 权限说明：
 *   - 管理员（ADMIN）：可新增、编辑、删除、发布/撤回公告
 *   - 所有用户：可查看已发布的公告列表和详情
 */
@RestController
@RequestMapping("/announcement")
@Slf4j
public class AnnouncementController {

    /** 公告业务逻辑层对象 */
    @Autowired
    private AnnouncementService announcementService;


    /**
     * 分页查询公告列表
     * 支持按标题模糊查询；通常用于后台管理页面展示所有公告
     * @param pageNum  当前页码（默认第1页）
     * @param pageSize 每页显示数量（默认10条）
     * @param announcement 查询条件对象（title支持模糊匹配）
     * @return 包含分页信息和公告列表的Result对象
     */
    @RequestMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             Announcement  announcement){
        log.info("公告分页查询请求: pageNum={}, pageSize={}, 查询条件={}", pageNum, pageSize, announcement);
        PageInfo<Announcement> pageInfo = announcementService.selectPage(pageNum, pageSize, announcement);
        log.info("公告分页查询结果: 总记录数={}, 当前页记录数={}", pageInfo.getTotal(), pageInfo.getList().size());
        return Result.success(pageInfo);
    }

    /**
     * 新增发布公告
     * 仅管理员可调用此接口；自动记录发布人和发布时间
     * 默认状态为"已发布"，也可通过参数指定为"待发布"实现草稿功能
     * @param announcement 公告对象（必须包含：title标题、content内容）
     * @return 操作结果（成功时返回新建的公告对象，包含数据库生成的ID）
     * @throws CustomerException 当标题为空时抛出异常
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Announcement announcement){
        Account currentUser = TokenUtils.getCurrentUser();
        if(!"ADMIN".equals(currentUser.getRole())){
            throw new CustomerException("只有管理员可以发布公告");
        }
        //校验必填字段：公告标题不能为空
        if(announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()){
            throw new CustomerException("标题不能为空");
        }
        log.info("新增公告请求: title={}, publisherId={}", announcement.getTitle(), announcement.getPublisherId());
        announcementService.add(announcement);
        log.info("新增公告成功: id={}, title={}", announcement.getId(), announcement.getTitle());
        return Result.success("200","发布成功",announcement);
    }

    /**
     * 更新公告信息
     * 可用于修改标题、内容或切换状态（如从"已撤回"改为"已发布"）
     * @param announcement 包含id和需要更新字段的实体对象
     * @return 操作结果
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Announcement announcement){
        log.info("更新公告请求: id={}, status={}", announcement.getId(), announcement.getStatus());
        announcementService.update(announcement);
        log.info("更新公告成功: id={}", announcement.getId());
        return Result.success("200","更新成功",announcement);
    }

    /**
     * 删除公告记录（物理删除）
     * 删除后不可恢复，建议前端提供二次确认提示
     * @param id 公告的主键ID
     * @return 操作结果
     */
    @RequestMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        log.info("删除公告请求: id={}", id);
        announcementService.deleteById(id);
        log.info("删除公告成功: id={}", id);
        return Result.success("200","删除成功",id);
    }

    /**
     * 根据ID查询公告详情
     * 用于编辑前加载数据或在弹窗中展示完整信息
     * @param id 公告的主键ID
     * @return 包含完整公告信息的Result对象
     */
    @RequestMapping("selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        log.info("查询公告详情请求: id={}", id);
        Announcement announcement = announcementService.selectById(id);
        return Result.success(announcement);
    }

    /**
     * 获取最新发布的公告列表（首页专用接口）
     * 只返回状态为"已发布"的公告，按发布时间倒序排列
     * 用于首页展示最新公告信息，提升用户体验
     * @param limit 返回的最大数量（默认5条，可根据页面布局调整）
     * @return 最新公告列表的Result对象
     */
    @RequestMapping("/latest")
    public Result latest(@RequestParam(defaultValue = "5") Integer limit){
        log.info("获取最新公告请求: limit={}", limit);
        List<Announcement> list = announcementService.getLatestAnnouncement(limit);
        log.info("获取最新公告成功: count={}", list.size());
        return Result.success(list);
    }

    /**
     * 获取公告统计数据（用于首页统计面板）
     * 统计维度包括：总数、已发布数、草稿/撤回数
     * 可扩展添加更多统计指标（如按月统计发布趋势等）
     * @return 包含统计数据的Map对象，key包括：total/published/draft
     */
    @RequestMapping("/statistics")
    public Result statistics() {
        log.info("获取公告统计数据请求");
        // 查询全部公告（不限状态）用于统计总数
        Announcement queryAll = new Announcement();
        List<Announcement> allList = announcementService.selectList(queryAll);

        // 查询已发布的公告数量
        Announcement queryPublished = new Announcement();
        queryPublished.setStatus("已发布");
        List<Announcement> publishedList = announcementService.selectList(queryPublished);

        // 封装统计结果到Map中返回
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", allList.size());           // 公告总数
        stats.put("published", publishedList.size());  // 已发布数量
        stats.put("draft", allList.size() - publishedList.size());  // 未发布（草稿+撤回）数量

        return Result.success(stats);
    }

}
