package com.lpo.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.lpo.util.ResponseUtil;

@WebServlet("/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("[UploadServlet] ===== 开始处理文件上传 =====");
        
        try {
            // 获取上传文件
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                System.out.println("[UploadServlet] 上传失败: 没有文件");
                ResponseUtil.error(resp, 400, "请选择要上传的文件");
                return;
            }

            // 获取文件名
            String submittedFileName = filePart.getSubmittedFileName();
            System.out.println("[UploadServlet] 原始文件名: " + submittedFileName + ", 文件大小: " + filePart.getSize());

            // 生成唯一文件名
            String ext = ".jpg";
            if (submittedFileName != null && submittedFileName.contains(".")) {
                ext = submittedFileName.substring(submittedFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + ext;
            
            // 确定上传目录：优先使用 web/uploads，否则使用系统临时目录
            String webRoot = getServletContext().getRealPath("/");
            System.out.println("[UploadServlet] webRoot: " + webRoot);
            
            File uploadDir;
            if (webRoot != null) {
                uploadDir = new File(webRoot, "uploads");
            } else {
                // 降级：使用 java.io.tmpdir + 项目名
                String tmpDir = System.getProperty("java.io.tmpdir");
                uploadDir = new File(tmpDir, "lpo_uploads");
                System.out.println("[UploadServlet] getRealPath返回null，使用临时目录: " + uploadDir.getAbsolutePath());
            }
            
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("[UploadServlet] 创建上传目录: " + uploadDir.getAbsolutePath() + " → " + (created ? "成功" : "失败"));
            }

            // 保存文件
            File destFile = new File(uploadDir, fileName);
            filePart.write(destFile.getAbsolutePath());
            System.out.println("[UploadServlet] 文件保存成功: " + destFile.getAbsolutePath());

            // 返回文件访问URL
            String fileUrl = "/uploads/" + fileName;
            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("fileName", fileName);
            
            System.out.println("[UploadServlet] ===== 上传成功, URL: " + fileUrl + " =====");
            ResponseUtil.success(resp, data);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[UploadServlet] 上传异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "文件上传失败: " + e.getMessage());
        }
    }
}