package com.mental.health.infra.storage;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地文件存储：封面图上传（jpg/png/webp ≤5MB），可切换 OSS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private final StorageProperties properties;

    /** 上传图片，返回对外访问 URL：/upload/xxx.png */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要上传的文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, "文件超出大小限制（≤5MB）");
        }
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED,
                    "文件类型不支持，仅支持 jpg/png/webp");
        }
        try {
            Path dir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("封面图上传成功: {}", target);
            return properties.getUrlPrefix() + "/" + filename;
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件保存失败");
        }
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
