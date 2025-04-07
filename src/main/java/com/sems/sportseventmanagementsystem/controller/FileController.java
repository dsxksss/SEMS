package com.sems.sportseventmanagementsystem.controller;

import com.sems.sportseventmanagementsystem.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("File upload request received, filename: " + 
                          (file != null ? file.getOriginalFilename() : "null") + 
                          ", size: " + (file != null ? file.getSize() : 0) + " bytes");
        
        try {
            if (file.isEmpty()) {
                System.out.println("文件上传失败: 文件为空");
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("请选择要上传的文件"));
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                System.out.println("文件上传失败: 不支持的文件类型 " + contentType);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("只支持上传图片文件"));
            }
            
            // 确保上传目录存在
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("创建上传目录: " + uploadPath.toAbsolutePath());
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 复制文件到上传目录
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件上传成功: " + filePath.toAbsolutePath());
            
            // 返回成功响应
            FileUploadResponse response = new FileUploadResponse(filename, file.getContentType(), file.getSize());
            System.out.println("返回响应: " + response.getFilename());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.out.println("文件上传异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("未预期的异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("服务器内部错误: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<?> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to load file: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error loading file: " + e.getMessage()));
        }
    }

    // Inner class for file upload response
    static class FileUploadResponse {
        private String filename;
        private String fileType;
        private long size;

        public FileUploadResponse(String filename, String fileType, long size) {
            this.filename = filename;
            this.fileType = fileType;
            this.size = size;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
} 