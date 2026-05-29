package com.noplay.similia.image.api.dto;

import com.noplay.similia.image.domain.Image;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [프론트엔드 참고용] 이미지 업로드 후 반환되는 응답 DTO
 * 업로드 성공 시 이 객체가 JSON 형태로 반환됩니다.
 *
 * [보안] 내부 순차 ID(Long)와 memberId는 노출하지 않습니다.
 *        imageToken(UUID)을 이미지 식별자로 사용하세요.
 */
@Getter
@Builder
public class ImageUploadResponseDto {

    /** 외부 공개용 이미지 식별자 (UUID) - 조회/삭제 시 이 값을 사용하세요. */
    private String imageToken;

    /** 사용자가 업로드한 원본 파일명 (예: photo.png) */
    private String originalName;

    /** 파일의 MIME 타입 (예: image/jpeg) */
    private String contentType;

    /** 파일의 용량 (Byte 단위) */
    private Long fileSize;

    /** 서버에 이미지가 저장(업로드)된 시각 */
    private LocalDateTime createdAt;

    // [추가된 부분 시작] 프론트엔드로 임베딩 벡터 값을 전달하기 위해 필드 추가
    /** AI 서버로부터 받아온 이미지 벡터(임베딩) 값 */
    private List<Double> embedding;

    public static ImageUploadResponseDto from(Image image, List<Double> embedding) {
        return ImageUploadResponseDto.builder()
                .imageToken(image.getImageToken())
                .originalName(image.getOriginalName())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .createdAt(image.getCreatedAt())
                .embedding(embedding)
                .build();
    }
    // [추가된 부분 끝]

    public static ImageUploadResponseDto from(Image image) {
        return from(image, null);
    }
}
