package com.example.service;

import com.example.entity.Announcement;
import com.example.mapper.AnnouncementMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告管理业务逻辑层
 * 处理公告的增删改查、发布/撤回切换、首页展示数据获取等业务逻辑
 */
@Service
public class AnnouncementService {

    /** 公告数据访问对象，用于数据库交互 */
    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 分页查询公告列表
     * 支持按标题模糊查询，按发布人ID、状态进行筛选
     * 查询结果按发布时间倒序排列（最新的在前）
     * @param pageNum  当前页码（从1开始）
     * @param pageSize 每页显示记录数
     * @param announcement 查询条件封装对象（可包含title/publisherId/status）
     * @return 分页结果对象，包含总记录数和当前页数据列表
     */
    public PageInfo<Announcement> selectPage(Integer pageNum , Integer pageSize, Announcement announcement){
        //开启分页查询插件
        PageHelper.startPage(pageNum, pageSize);
        List<Announcement> list = announcementMapper.selectList(announcement);
        return PageInfo.of(list);
    }

    /**
     * 新增公告
     * 自动设置发布时间戳；若未指定状态则默认设为"已发布"
     * 调用前需确保publisherId已正确设置（通常从前端传入当前登录用户信息）
     *
     * @param announcement 公告实体对象（必须包含title、content、publisherId）
     */
    public void add(Announcement announcement){
        //若未指定状态，默认设置为"已发布"
        if(announcement.getStatus() == null || announcement.getStatus().isEmpty()){
            announcement.setStatus("已发布");
        }
        //设置发布时间
        announcement.setPublishTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
    }

    /**
     * 更新公告信息
     * 支持部分字段更新，可用于修改标题、内容或切换状态（发布/撤回）
     * @param announcement 包含id和需要更新字段的实体对象
     */
    public void update(Announcement announcement){
        announcementMapper.update(announcement);
    }

    /**
     * 删除公告记录（物理删除）
     * 删除后不可恢复，调用前建议在Controller层添加确认提示
     * @param id 公告的主键ID
     */
    public void deleteById(Integer id) {
        announcementMapper.deleteById(id);
    }

    /**
     * 根据主键ID查询公告详情
     * 用于编辑前加载数据或查看详情弹窗场景
     * @param id 公告的主键ID
     * @return 公告完整对象，不存在时返回null
     */
    public Announcement selectById(Integer id){
        // 构建查询条件
        Announcement query = new Announcement();
        query.setId(id);
        // 执行查询并返回第一条结果
        List<Announcement> list = announcementMapper.selectList(query);
        return list.isEmpty() ? null : list.get(0);
    }


    /**
     * 获取最新发布的公告列表（用于首页展示）
     * 只返回状态为"已发布"的公告，按发布时间倒序排列
     * @param limit 返回的最大记录数量（建议5-10条）
     * @return 最新公告列表，数量不超过limit参数指定值
     */
    public List<Announcement> getLatestAnnouncement(int limit){
        // 限制只查第一页，每页limit条记录
        PageHelper.startPage(1, limit);
        // 构建查询条件：只查询已发布的公告
        Announcement query = new Announcement();
        query.setStatus("已发布");
        return announcementMapper.selectList(query);
    }

    /**
     * 根据条件查询公告列表（不分页）
     * 用于统计等场景，支持任意条件组合查询
     * @param announcement 查询条件对象
     * @return 符合条件的公告列表
     */
    public List<Announcement> selectList(Announcement announcement) {
        return announcementMapper.selectList(announcement);
    }
}

