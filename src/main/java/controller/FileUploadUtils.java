package controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import service.exception.BadRequestException;

public class FileUploadUtils {
    public static final String URL_PREFIX = "/uploads/";

    private static Path uploadRoot =
            Paths.get(System.getProperty("user.home"), "campusmarket-uploads");

    public static void setUploadRoot(String dir) {
        if (dir != null && !dir.isBlank()) {
            uploadRoot = Paths.get(dir);
        }
    }

    public static Path getUploadRoot() {
        return uploadRoot;
    }

    public static String saveImage(Part part, String category)
            throws IOException, BadRequestException {
        if (part == null || part.getSize() == 0) {
            throw new BadRequestException("사진을 선택해 주세요");
        }
        String contentType = part.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("사진 파일만 올릴 수 있어요");
        }

        String fileName = UUID.randomUUID().toString() + getExtension(part.getSubmittedFileName());
        Path dir = uploadRoot.resolve(category);
        Files.createDirectories(dir);
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
        return URL_PREFIX + category + "/" + fileName;
    }

    public static String toUrl(HttpServletRequest request, String path) {
        if (path == null || path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        return request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getContextPath()
                + path;
    }

    public static Path resolve(String relativePath) {
        Path target = uploadRoot.resolve(relativePath).normalize();
        if (!target.startsWith(uploadRoot.normalize())) {
            return null;
        }
        return target;
    }

    private static String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        String extension = fileName.substring(dot).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,5}") ? extension : "";
    }
}
