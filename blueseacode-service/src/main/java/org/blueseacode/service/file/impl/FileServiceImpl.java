package org.blueseacode.service.file.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.service.file.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final OSS ossClient;

    @Value("${oss.bucket-name}")
    private String bucketName;

    @Value("${oss.endpoint}")
    private String endpoint;

    /** 允许上传的图片扩展名 */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );

    /** 允许上传的通用文件扩展名 */
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "rar", "7z", "tar", "gz",
            "txt", "md",
            "mp4", "avi", "mov", "wmv",
            "mp3", "wav",
            "java", "py", "js", "ts", "html", "css", "xml", "json", "sql",
            "exe", "msi", "apk", "dmg"
    );

    @Override
    public String upload(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "上传文件不能为空");
        }

        // 校验文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED);
        }

        // 生成唯一文件名，防止覆盖
        String objectName = buildObjectName(directory, originalFilename);

        try (InputStream inputStream = file.getInputStream()) {
            // 设置Content-Type，确保浏览器能正确识别和显示文件
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getMimeType(extension));
            metadata.setContentLength(file.getSize());
            // 强制设置为 inline，覆盖 OSS 桶的强制下载策略（x-oss-force-download: true）
            metadata.setContentDisposition("inline");

            ossClient.putObject(bucketName, objectName, inputStream, metadata);

            // 设置文件为公共读权限，确保浏览器可以访问
            ossClient.setObjectAcl(bucketName, objectName, CannedAccessControlList.PublicRead);

            log.info("OSS文件上传成功: bucket={}, object={}", bucketName, objectName);
        } catch (IOException e) {
            log.error("OSS文件上传失败: {}", originalFilename, e);
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "文件上传失败");
        }

        // 返回可公开访问的URL
        return buildFileUrl(objectName);
    }

    @Override
    public void delete(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // 去除开头的斜杠得到 objectName
            String objectName = path.startsWith("/") ? path.substring(1) : path;
            ossClient.deleteObject(bucketName, objectName);
            log.info("OSS文件删除成功: bucket={}, object={}", bucketName, objectName);
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", fileUrl, e);
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "文件删除失败");
        }
    }

    @Override
    public String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 构建OSS对象存储路径
     */
    private String buildObjectName(String directory, String originalFilename) {
        String ext = getExtension(originalFilename);
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        if (directory != null && !directory.isEmpty()) {
            return directory + "/" + uniqueName;
        }
        return uniqueName;
    }

    /**
     * 构建文件的公开访问URL
     * <p>
     * 对于图片类型，生成带 {@code response-content-disposition=inline} 的签名URL（10年有效期），
     * 以覆盖 OSS 存储桶的强制下载策略，确保图片在浏览器中正常显示而非触发下载。
     * 非图片文件仍使用公共读URL（触发下载为预期行为）。
     */
    private String buildFileUrl(String objectName) {
        String extension = getExtension(objectName).toLowerCase();
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            // 图片类型 → 签名URL + inline，覆盖桶级别强制下载策略
            return buildPresignedUrl(objectName, "inline");
        }
        // 非图片文件使用公共读URL
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    /**
     * 生成签名URL并覆盖响应头，优先级高于 OSS 桶级默认策略
     *
     * @param objectName          OSS对象名称
     * @param contentDisposition  内容处置方式（inline / attachment）
     * @return 签名URL（含 response-content-disposition 参数）
     */
    private String buildPresignedUrl(String objectName, String contentDisposition) {
        // 10年有效期，对头像/封面等长期资源足够
        Date expiration = new Date(System.currentTimeMillis() + 10 * 365L * 24 * 3600 * 1000L);

        ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
        responseHeaders.setContentDisposition(contentDisposition);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                bucketName, objectName, com.aliyun.oss.HttpMethod.GET);
        request.setExpiration(expiration);
        request.setResponseHeaders(responseHeaders);

        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * 根据文件扩展名获取MIME类型
     */
    private String getMimeType(String extension) {
        if (extension == null) return "application/octet-stream";
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "svg": return "image/svg+xml";
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip": return "application/zip";
            case "rar": return "application/vnd.rar";
            case "7z": return "application/x-7z-compressed";
            case "tar": return "application/x-tar";
            case "gz": return "application/gzip";
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "wmv": return "video/x-ms-wmv";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            case "txt": return "text/plain";
            case "md": return "text/markdown";
            case "html": return "text/html";
            case "css": return "text/css";
            case "js": return "text/javascript";
            case "ts": return "text/typescript";
            case "json": return "application/json";
            case "xml": return "application/xml";
            case "sql": return "text/plain";
            default: return "application/octet-stream";
        }
    }
}
