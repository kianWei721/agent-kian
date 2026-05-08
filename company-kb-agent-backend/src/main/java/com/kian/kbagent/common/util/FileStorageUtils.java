package com.kian.kbagent.common.util;

import com.kian.kbagent.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class FileStorageUtils {

    private FileStorageUtils() {
    }

    public static String resolveExtension(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("文件缺少扩展名");
        }
        return extension.toLowerCase();
    }

    public static Path buildStoragePath(String uploadDir, String originalFilename) {
        String fileName = Paths.get(StringUtils.cleanPath(originalFilename)).getFileName().toString();
        String extension = resolveExtension(fileName);
        String baseName = fileName.replaceAll("\\.[^.]+$", "");
        String safeName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_");
        String finalFileName = safeName + "-" + UUID.randomUUID() + "." + extension;
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(finalFileName);
    }
}
