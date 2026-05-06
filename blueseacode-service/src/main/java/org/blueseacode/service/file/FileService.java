package org.blueseacode.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件到阿里云OSS
     *
     * @param file      上传的文件
     * @param directory 目录前缀（如 avatar, resource, article）
     * @return 文件的可访问URL
     */
    String upload(MultipartFile file, String directory);

    /**
     * 从OSS删除文件
     *
     * @param fileUrl 文件的完整URL
     */
    void delete(String fileUrl);

    /**
     * 获取文件扩展名
     */
    String getExtension(String filename);
}
