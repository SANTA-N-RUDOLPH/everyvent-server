package kr.santanrudolph.everyvent.infrastructure.s3;


import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private static final long MB = 1024 * 1024;

  private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
      "jpg", "image/jpeg",
      "jpeg", "image/jpeg",
      "png", "image/png"
  );

  @Value("${aws.s3.bucket-name}")
  private String bucketName;

  @Value("${aws.s3.presigned-url-expiration}")
  private long presignedUrlExpiration;

  @Value("${aws.s3.max-file-size}")
  private long maxFileSize;

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;


  public ProfileImageUploadResponse generatePresignedResponse(Long userId, String originalFilename,
                                                              String contentType, long fileSize) {
    validateFileSize(fileSize);

    String extension = extractExtension(originalFilename).toLowerCase();
    validateExtension(extension);
    validateContentType(contentType, extension);

    String objectKey = generateObjectKey(userId, extension);
    String presignedUrl = generatePresignedUrl(objectKey, contentType, fileSize);

    log.info("Generated presigned PUT URL - User ID: {}, Object Key: {}, Expiration: {}s",
        userId, objectKey, presignedUrlExpiration);

    return new ProfileImageUploadResponse(presignedUrl, objectKey, presignedUrlExpiration);
  }

  public void deleteObject(String objectKey, Long userId) {
    validateObjectKey(objectKey, userId);

    try {
      DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(objectKey)
          .build();

      s3Client.deleteObject(deleteRequest);
      log.info("Deleted S3 object - Key: {}", objectKey);
    } catch (S3Exception e) {
      log.error("Failed to delete S3 object - Key: {}", objectKey, e);
    }
  }

  public void validateObjectKey(String objectKey, Long userId) {
    if (objectKey == null || objectKey.contains("..") || objectKey.startsWith("/")) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "유효하지 않은 파일 경로입니다.");
    }

    String allowedPrefix = "users/" + userId + "/profile/";

    if (!objectKey.startsWith(allowedPrefix)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "접근 권한이 없는 파일입니다.");
    }
  }

  private String generatePresignedUrl(String objectKey, String contentType, long fileSize) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(objectKey)
        .contentType(contentType)
        .contentLength(fileSize)
        .build();

    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
        .putObjectRequest(putObjectRequest)
        .build();

    return s3Presigner.presignPutObject(presignRequest).url().toString();
  }

  private String generateObjectKey(Long userId, String extension) {
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String uuid = UUID.randomUUID().toString().substring(0, 8);
    return String.format("users/%d/profile/%s_%s.%s",
        userId, timestamp, uuid, extension);
  }

  private void validateFileSize(long fileSize) {
    if (fileSize > maxFileSize) {
      throw new EveryventException(ErrorCode.INVALID_INPUT,
          String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / MB));
    }
  }

  private void validateExtension(String extension) {
    if (!EXTENSION_TO_CONTENT_TYPE.containsKey(extension)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT,
          String.format("지원하지 않는 파일 형식입니다. (지원: %s)",
              String.join(", ", EXTENSION_TO_CONTENT_TYPE.keySet())));
    }
  }

  private void validateContentType(String contentType, String extension) {
    String expectedContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
    if (!contentType.equalsIgnoreCase(expectedContentType)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT,
          "파일 형식과 Content-Type이 일치하지 않습니다.");
    }
  }

  private String extractExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "파일 확장자가 없습니다.");
    }
    return filename.substring(filename.lastIndexOf(".") + 1);
  }
}
