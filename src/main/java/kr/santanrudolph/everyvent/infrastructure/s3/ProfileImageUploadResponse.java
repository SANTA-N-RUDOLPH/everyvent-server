package kr.santanrudolph.everyvent.infrastructure.s3;


public record ProfileImageUploadResponse(
    String presignedUrl,
    String objectKey,
    long expiresIn
) {
}
