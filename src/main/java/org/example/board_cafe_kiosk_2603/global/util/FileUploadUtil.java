package org.example.board_cafe_kiosk_2603.global.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 이미지 파일 업로드 유틸리티
 * application.properties의 my.upload.path 경로에 저장
 */
@Log4j2
@Component
public class FileUploadUtil {

    private static final int THUMBNAIL_MAX_WIDTH = 640;
    private static final int THUMBNAIL_MAX_HEIGHT = 360;
    private static final String THUMBNAIL_PREFIX = "thumb_";

    // 파일 삭제를 위해서 추가
    @Value("${my.upload.path}")  // MAC 설정 되어있음
    private String uploadPath;  // 파일의 저장 경로

    /**
     * 이미지 파일을 서버에 저장하고 접근 가능한 URL 경로를 반환
     *
     * @param file 업로드된 MultipartFile
     * @return 저장된 파일의 URL 경로 (예: /upload/uuid_filename.jpg)
     */
    public String save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.debug("업로드 파일 없음 - null 반환");
            return null;
        }

        // 저장 디렉토리 생성
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
            log.debug("업로드 디렉토리 생성: {}", uploadPath);
        }

        // UUID로 고유 파일명 생성
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String savedName = UUID.randomUUID().toString() + ext;
        String thumbnailName = THUMBNAIL_PREFIX + savedName;

        // 파일 저장
        File savedFile = new File(uploadPath, savedName);
        file.transferTo(savedFile);
        log.debug("파일 저장 완료: {}", savedFile.getAbsolutePath());

        File thumbnailFile = new File(uploadPath, thumbnailName);
        if (createThumbnail(savedFile, thumbnailFile, ext)) {
            log.debug("썸네일 저장 완료: {}", thumbnailFile.getAbsolutePath());
            return "/upload/" + thumbnailName;
        }

        log.debug("썸네일 생성 불가 - 원본 경로 반환: {}", savedFile.getAbsolutePath());
        return "/upload/" + savedName;
    }

    /**
     * 기존 파일 삭제
     *
     * @param imageUrl DB에 저장된 URL 경로
     */
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        String fileName = imageUrl.replace("/upload/", "");
        deleteFile(new File(uploadPath, fileName));

        if (fileName.startsWith(THUMBNAIL_PREFIX)) {
            deleteFile(new File(uploadPath, fileName.substring(THUMBNAIL_PREFIX.length())));
        } else {
            deleteFile(new File(uploadPath, THUMBNAIL_PREFIX + fileName));
        }
    }

    private boolean createThumbnail(File originalFile, File thumbnailFile, String ext) {
        try {
            BufferedImage originalImage = ImageIO.read(originalFile);
            if (originalImage == null) {
                return false;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            double scale = Math.min(
                    (double) THUMBNAIL_MAX_WIDTH / originalWidth,
                    (double) THUMBNAIL_MAX_HEIGHT / originalHeight
            );
            scale = Math.min(scale, 1.0d);

            int targetWidth = Math.max(1, (int) Math.round(originalWidth * scale));
            int targetHeight = Math.max(1, (int) Math.round(originalHeight * scale));
            int imageType = supportsTransparency(ext) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage thumbnailImage = new BufferedImage(targetWidth, targetHeight, imageType);
            Graphics2D graphics = thumbnailImage.createGraphics();

            try {
                if (!supportsTransparency(ext)) {
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(0, 0, targetWidth, targetHeight);
                }
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            } finally {
                graphics.dispose();
            }

            String formatName = normalizeFormat(ext);
            return ImageIO.write(thumbnailImage, formatName, thumbnailFile);
        } catch (IOException e) {
            log.warn("썸네일 생성 실패 - 원본 경로 사용: {}", originalFile.getAbsolutePath(), e);
            return false;
        }
    }

    private String normalizeFormat(String ext) {
        String format = ext.replace(".", "").toLowerCase();
        return switch (format) {
            case "jpeg" -> "jpg";
            default -> format;
        };
    }

    private boolean supportsTransparency(String ext) {
        String format = normalizeFormat(ext);
        return "png".equals(format) || "gif".equals(format) || "webp".equals(format);
    }

    private void deleteFile(File file) {
        if (file.exists() && file.delete()) {
            log.debug("파일 삭제 완료: {}", file.getAbsolutePath());
        }
    }
}
