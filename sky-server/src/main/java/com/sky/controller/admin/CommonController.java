package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j

public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    // 文件上歘
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传:{}",file);
        try {
            // 截取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取原始文件的后缀
            String substring = originalFilename.substring(originalFilename.lastIndexOf('.'));
            // 构建新的文件名称
            String objectName = UUID.randomUUID().toString() + substring;

            String upload = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(upload);
        } catch (IOException e) {
            log.error("文件上传失败");
        }
        return Result.error("文件上传失败");
    }
}
