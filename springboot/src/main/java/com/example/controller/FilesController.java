package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import com.example.exception.CustomerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * 文件管理控制器
 * 处理文件的上传和下载操作，主要用于用户头像等静态资源的管理
 * 文件存储位置：项目根目录下的files文件夹
 * 访问路径示例：
 *   - 上传接口：POST /file/upload
 *   - 下载接口：GET /file/download/{fileName}
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FilesController {

    /**
     * 下载文件
     * 根据文件名从服务器files目录读取文件并返回给客户端
     * 支持任意格式文件的下载（图片、文档等）
     *
     * 使用示例：
     *   前端可直接通过URL访问：<img src="http://localhost:8083/file/download/avatar.jpg" />
     *
     * @param fileName 要下载的文件名称（包含扩展名）
     * @param response HTTP响应对象，用于输出文件流
     * @throws Exception 当文件不存在时抛出CustomerException异常
     */
    @RequestMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws Exception {
        // 构建文件存储的绝对路径：项目根目录/files/
        String filePath = System.getProperty("user.dir") + "/files/";
        String realPath = filePath + fileName;

        log.info("文件下载请求: fileName={}", fileName);
        // 校验文件是否存在，不存在则抛出异常提示前端
        boolean exist = FileUtil.exist(realPath);
        if (!exist) {
            throw new CustomerException("文件不存在");
        }
        // 读取文件内容到字节数组（适用于小文件，大文件建议使用流式传输）
        byte[] bytes = FileUtil.readBytes(realPath);
        // 获取响应输出流，将文件字节写入响应体返回给客户端
        ServletOutputStream os = response.getOutputStream();
        // 输出流对象把文件写出到客户端
        os.write(bytes);
        os.flush(); // 刷新缓冲区确保数据全部写出
        os.close(); // 关闭流释放资源
        log.info("文件下载成功: fileName={}, size={} bytes", fileName, bytes.length);
    }

/**
 * 上传文件
 * 接收前端提交的文件，保存到服务器本地文件系统
 * 文件命名规则：时间戳_原始文件名（避免重名覆盖）
 *
 * 功能特点：
 *   - 自动创建存储目录（如不存在）
 *   - 重命名防止文件名冲突
 *   - 返回可访问的完整URL地址
 *
 * @param file 前端上传的文件对象（表单字段名为"file"）
 * @return 包含文件访问URL的Result对象，格式："http://localhost:8083/file/download/{新文件名}"
 * @throws Exception 文件读写异常
 */
    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("文件上传请求: originalFilename={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        // 确定文件存储目录：项目根目录/files/
        String filePath = System.getProperty("user.dir") + "/files/";

        // 如果存储目录不存在则自动创建
        if (!FileUtil.isDirectory(filePath)) {
            FileUtil.mkdir(filePath);
        }
        // 获取文件内容的字节数组
        byte[] bytes = file.getBytes();

        // 生成唯一文件名：当前时间戳(毫秒)_原文件名
        // 时间戳确保同一秒内上传多个文件不会重名
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();  // 文件的原始名称
        // 将文件写入磁盘
        FileUtil.writeBytes(bytes, filePath + fileName);

        // 构建可访问的完整URL路径返回给前端
        String url = "http://localhost:8083/file/download/" + fileName;
        log.info("文件上传成功: fileName={}, url={}", fileName, url);
        return Result.success(url);
    }

}

