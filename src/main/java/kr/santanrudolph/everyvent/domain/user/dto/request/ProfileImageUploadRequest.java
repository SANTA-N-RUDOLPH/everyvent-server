package kr.santanrudolph.everyvent.domain.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProfileImageUploadRequest(

    @NotBlank(message = "파일명은 필수입니다.")
    String filename,

    @NotBlank(message = "파일 타입은 필수입니다.")
    String contentType,

    @NotNull(message = "파일 크기는 필수입니다.")
    @Positive(message = "파일 크기는 0보다 커야 합니다.")
    Long fileSize
) {
}
