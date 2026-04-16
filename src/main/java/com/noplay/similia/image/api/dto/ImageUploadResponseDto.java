package com.noplay.similia.image.api.dto;

import com.noplay.similia.image.domain.Image;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [프론트엔드 참고용] 이미지 업로드 후 반환되는 응답 DTO
 * 업로드 성공 시 이 객체가 JSON 형태로 반환됩니다.
 */
@Getter
@Builder
public class ImageUploadResponseDto {
    
    /** 업로드된 이미지 고유 ID (식별자) -> 차후 이미지를 불러오거나 추천을 요청할 때 사용합니다. */
    private Long id;
    
    /** 이미지를 업로드한 회원의 ID */
    private Long memberId;
    
    /** 사용자가 업로드한 원본 파일명 (예: photo.png) */
    private String originalName;
    
    /** 파일의 MIME 타입 (예: image/jpeg) */
    private String contentType;
    
    /** 파일의 용량 (Byte 단위) */
    private Long fileSize;
    
    /** 서버에 이미지가 저장(업로드)된 시각 */
    private LocalDateTime createdAt;

    public static ImageUploadResponseDto from(Image image) {
        return ImageUploadResponseDto.builder()
                .id(image.getId())
                .memberId(image.getMemberId())
                .originalName(image.getOriginalName())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
