package com.noplay.similia.image.application;

import com.noplay.similia.global.exception.BusinessException;
import com.noplay.similia.global.exception.ErrorCode;
import com.noplay.similia.image.api.dto.ImageUploadResponseDto;
import com.noplay.similia.image.domain.Image;
import com.noplay.similia.image.infrastructure.ImageRepository;
import com.noplay.similia.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 이미지 관리를 담당하는 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 프론트엔드에서 전달받은 이미지 파일을 DB(BYTEA)에 저장합니다.
     * @param memberId 회원 ID (유효성 검사 진행)
     * @param file 업로드된 이미지 파일 (용량, 타입 검사 진행)
     * @return 저장 완료된 이미지 정보 DTO
     */
    @Transactional
    public ImageUploadResponseDto upload(Long memberId, MultipartFile file) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_ID);
        }

        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        try {
            Image image = Image.builder()
                    .memberId(memberId)
                    .originalName(file.getOriginalFilename())
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .data(file.getBytes())
                    .build();

            Image savedImage = imageRepository.save(image);
            return ImageUploadResponseDto.from(savedImage);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public Image findById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
    }

    @Transactional
    public void delete(Long memberId, Long imageId) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_ID);
        }

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        if (!image.getMemberId().equals(memberId)) {
            // 다른 사람의 이미지를 삭제하려고 할 때 권한 없음(403 혹은 커스텀 에러) 대신 
            // 현재는 간단히 NOT_FOUND 처리를 하거나, INVALID_INPUT_VALUE를 사용할 수 있습니다.
            // 더 정확히 하려면 ErrorCode.ACCESS_DENIED 등을 만드는 것이 좋습니다.
            throw new IllegalArgumentException("본인의 이미지만 삭제할 수 있습니다.");
        }

        imageRepository.delete(image);
    }
}
