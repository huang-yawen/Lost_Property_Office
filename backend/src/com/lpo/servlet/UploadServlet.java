package com.lpo.servlet;

import com.lpo.util.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@WebServlet("/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class UploadServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 验证token
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        Integer userId = JwtUtil.getUserId(token);
        if (userId == null) {
            ResponseUtil.unauthorized(resp);
            return;
        }

        try {
            // 获取上传文件
            Part filePart = req.getPart("file");
            if (filePart == null) {
                ResponseUtil.error(resp, 400, "请选择要上传的文件");
                return;
            }

            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String ext = "";
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0) {
                ext = fileName.substring(dotIndex);
            }

            // 生成唯一文件名
            String newFileName = UUID.randomUUID().toString() + ext;

            // 获取上传目录路径（相对于web应用）
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            String filePath = uploadPath + File.separator + newFileName;
            filePart.write(filePath);

            // 返回文件访问URL
            String fileUrl = "/" + UPLOAD_DIR + "/" + newFileName;

            Map<String, Object> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("fileName", fileName);
            ResponseUtil.success(resp, data);

        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "文件上传失败");
        }
    }
}