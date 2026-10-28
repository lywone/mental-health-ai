package com.mental.health.infra.storage;

import com.mental.health.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传接口（管理后台）
 */
@Tag(name = "上传-管理端")
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storageService;

    public record UploadVO(String url) {
    }

    @Operation(summary = "封面图上传（jpg/png/webp ≤5MB）")
    @PostMapping("/image")
    public Result<UploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.ok(new UploadVO(storageService.uploadImage(file)));
    }
}
