package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
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
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("上传文件:{}",file);
        // 把文件上传到阿里云
        try {
            // 文件的原始文件名
            String originalFilename = file.getOriginalFilename();
            // 截取文件名后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String objectName = UUID.randomUUID().toString() + extension;
            // 上传文件，并获得路径
            String path = aliOssUtil.upload(file.getBytes(), objectName);

            return Result.success(path);
        } catch (IOException e) {
            log.info("文件上传失败");
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
