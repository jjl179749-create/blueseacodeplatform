package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.Result;
import org.blueseacode.service.file.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 通用文件上传
     *
     * @param file      上传的文件
     * @param directory 存储目录（可选，如 avatar/resource/article）
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "common") String directory) {
        // 获取当前用户ID（无需显式使用，但确保已认证）
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        String url = fileService.upload(file, directory);
        return Result.success("上传成功", url);
    }

    /**
     * 上传头像专用接口
     * 上传后自动更新用户头像
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        String url = fileService.upload(file, "avatar");
        return Result.success("头像上传成功", url);
    }
}
